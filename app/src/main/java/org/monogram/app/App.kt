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
    lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        initCrashHandler()
        startupInitializer.initialize()
        initAppContainer()
        initMapLibre()
        checkPushAvailability()
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

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return imageLoader
    }
}
