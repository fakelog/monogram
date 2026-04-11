package org.monogram.app

import android.app.Application
import android.content.Intent
import android.util.Log
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.monogram.app.di.HiltAppContainer
import org.monogram.app.di.StartupInitializer
import org.monogram.data.infra.DataMemoryPressureHandler
import org.monogram.domain.managers.DistrManager
import org.monogram.domain.repository.AppPreferencesProvider
import org.monogram.domain.repository.PushProvider
import org.monogram.presentation.di.AppContainer
import javax.inject.Inject
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

@HiltAndroidApp
class App : Application(), SingletonImageLoader.Factory {
    lateinit var appContainer: AppContainer

    @Inject
    lateinit var startupInitializer: StartupInitializer

    @Inject
    lateinit var distrManager: DistrManager

    @Inject
    lateinit var appPreferencesProvider: AppPreferencesProvider

    @Inject
    lateinit var dataMemoryPressureHandler: DataMemoryPressureHandler

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        initCrashHandler()
        startupInitializer.initialize()
        initAppContainer()
        initMapLibre()
        checkPushAvailability()
    }

    @Suppress("DEPRECATION")
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_RUNNING_LOW) {
            trimInMemoryCaches("onTrimMemory:$level")
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        trimInMemoryCaches("onLowMemory")
    }

    private fun initCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                val stackTrace = sw.toString()

                Log.d("CrashHandler", stackTrace)

                val intent = Intent(this, CrashActivity::class.java).apply {
                    putExtra("EXTRA_CRASH_LOG", stackTrace)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
                exitProcess(1)
            } catch (e: Exception) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun initAppContainer() {
        appContainer = HiltAppContainer(this)
    }

    private fun initMapLibre() {
        MapLibre.getInstance(this, null, WellKnownTileServer.MapLibre)
    }

    private fun checkPushAvailability() {
        val isGmsAvailable = distrManager.isGmsAvailable()
        val isFcmAvailable = distrManager.isFcmAvailable()

        if (!(isGmsAvailable && isFcmAvailable) && appPreferencesProvider.pushProvider.value == PushProvider.FCM) {
            appPreferencesProvider.setPushProvider(PushProvider.GMS_LESS)
        }
    }

    private fun trimInMemoryCaches(reason: String) {
        if (!::appContainer.isInitialized) return
        runCatching {
            dataMemoryPressureHandler.clearDataCaches(reason)
        }.onFailure { error ->
            Log.w(TAG, "Failed to clear data caches for $reason", error)
        }

        runCatching {
            imageLoader.memoryCache?.clear()
        }.onFailure { error ->
            Log.w(TAG, "Failed to clear Coil memory cache for $reason", error)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return imageLoader
    }

    companion object {
        private const val TAG = "App"
    }
}
