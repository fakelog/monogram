package org.monogram.app.di

import android.content.ClipboardManager
import android.content.Context
import android.telephony.TelephonyManager
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.monogram.core.Logger
import org.monogram.domain.managers.*
import org.monogram.domain.repository.*
import org.monogram.presentation.core.util.*
import org.monogram.presentation.features.chats.currentChat.components.ExoPlayerCache
import org.monogram.presentation.features.chats.currentChat.components.VideoPlayerPool
import org.monogram.presentation.settings.storage.CacheController
import org.monogram.presentation.di.coil.LottieDecoder
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppPreferences(
        @ApplicationContext context: Context,
        appCoroutineScope: kotlinx.coroutines.CoroutineScope,
    ): AppPreferences = AppPreferences(context, appCoroutineScope)

    @Provides
    @Singleton
    fun provideAppPreferencesProvider(appPreferences: AppPreferences): AppPreferencesProvider = appPreferences

    @Provides
    @Singleton
    fun provideEditorSnippetProvider(@ApplicationContext context: Context): EditorSnippetProvider =
        EditorSnippetPreferences(context)

    @Provides
    @Singleton
    fun provideCacheProvider(@ApplicationContext context: Context): CacheProvider =
        CachePreferences(context)

    @Provides
    @Singleton
    fun provideBotPreferencesProvider(@ApplicationContext context: Context): BotPreferencesProvider =
        BotPreferences(context)

    @Provides
    @Singleton
    fun provideExoPlayerCache() = ExoPlayerCache()

    @Provides
    @Singleton
    fun provideCacheController(
        @ApplicationContext context: Context,
        exoPlayerCache: ExoPlayerCache,
    ) = CacheController(context, exoPlayerCache)

    @Provides
    @Singleton
    fun provideVideoPlayerPool(
        @ApplicationContext context: Context,
        exoPlayerCache: ExoPlayerCache,
        playerDataSourceFactory: PlayerDataSourceFactory,
    ) = VideoPlayerPool(context, exoPlayerCache, playerDataSourceFactory)

    @Provides
    @Singleton
    fun provideClipManager(@ApplicationContext context: Context): ClipManager {
        return ClipManagerImpl(
            context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager,
        )
    }

    @Provides
    @Singleton
    fun provideLogger(): Logger = LoggerImpl()

    @Provides
    @Singleton
    fun provideDateFormatManager(@ApplicationContext context: Context): DateFormatManager =
        SystemDateFormatManager(context)

    @Provides
    fun providePhoneManager(@ApplicationContext context: Context): PhoneManager {
        return PhoneManagerImpl(
            context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager,
        )
    }

    @Provides
    fun provideDomainManager(
        @ApplicationContext context: Context,
        externalNavigator: ExternalNavigator,
    ): DomainManager = DomainManagerImpl(context, externalNavigator.packageName)

    @Provides
    fun provideAssetsManager(@ApplicationContext context: Context): AssetsManager = AssetsManagerImpl(context)

    @Provides
    fun provideDistrManager(@ApplicationContext context: Context): DistrManager = DistrManagerImpl(context)

    @Provides
    fun provideMessageDisplayer(@ApplicationContext context: Context): MessageDisplayer =
        ToastMessageDisplayer(context)

    @Provides
    fun provideExternalNavigator(@ApplicationContext context: Context): ExternalNavigator =
        ExternalNavigatorImpl(context)

    @Provides
    fun provideDownloadUtils(
        @ApplicationContext context: Context,
        messageDisplayer: MessageDisplayer,
    ): IDownloadUtils = DownloadUtils(context, messageDisplayer)

    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(LottieDecoder.Factory())
                add(SvgDecoder.Factory())
                add(OkHttpNetworkFetcherFactory())
            }
            .build()
    }
}
