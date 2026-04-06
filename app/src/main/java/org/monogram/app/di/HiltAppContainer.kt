package org.monogram.app.di

import android.content.Context
import coil3.ImageLoader
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import org.monogram.core.DispatcherProvider
import org.monogram.core.Logger
import org.monogram.domain.managers.*
import org.monogram.domain.repository.*
import org.monogram.presentation.core.util.AppPreferences
import org.monogram.presentation.core.util.IDownloadUtils
import org.monogram.presentation.di.AppContainer
import org.monogram.presentation.di.PreferencesContainer
import org.monogram.presentation.di.RepositoriesContainer
import org.monogram.presentation.di.UtilsContainer
import org.monogram.presentation.features.chats.currentChat.components.ExoPlayerCache
import org.monogram.presentation.features.chats.currentChat.components.VideoPlayerPool
import org.monogram.presentation.settings.storage.CacheController

class HiltAppContainer(context: Context) : AppContainer {
    private val entryPoint = EntryPointAccessors.fromApplication(
        context,
        AppContainerEntryPoint::class.java,
    )

    override val preferences: PreferencesContainer = HiltPreferencesContainer(entryPoint)
    override val cacheProvider: CacheProvider by lazy { entryPoint.cacheProvider() }
    override val repositories: RepositoriesContainer = HiltRepositoriesContainer(entryPoint)
    override val utils: UtilsContainer = HiltUtilsContainer(entryPoint)
}

private class HiltPreferencesContainer(
    private val entryPoint: AppContainerEntryPoint,
) : PreferencesContainer {
    override val appPreferences: AppPreferences by lazy { entryPoint.appPreferences() }
    override val appPreferencesProvider: AppPreferencesProvider by lazy { entryPoint.appPreferencesProvider() }
    override val botPreferencesProvider: BotPreferencesProvider by lazy { entryPoint.botPreferencesProvider() }
    override val editorSnippetProvider: EditorSnippetProvider by lazy { entryPoint.editorSnippetProvider() }
}

private class HiltRepositoriesContainer(
    private val entryPoint: AppContainerEntryPoint,
) : RepositoriesContainer {
    override val authRepository: AuthRepository by lazy { entryPoint.authRepository() }
    override val chatListRepository: ChatListRepository by lazy { entryPoint.chatListRepository() }
    override val chatFolderRepository: ChatFolderRepository by lazy { entryPoint.chatFolderRepository() }
    override val chatOperationsRepository: ChatOperationsRepository by lazy { entryPoint.chatOperationsRepository() }
    override val chatSearchRepository: ChatSearchRepository by lazy { entryPoint.chatSearchRepository() }
    override val forumTopicsRepository: ForumTopicsRepository by lazy { entryPoint.forumTopicsRepository() }
    override val chatSettingsRepository: ChatSettingsRepository by lazy { entryPoint.chatSettingsRepository() }
    override val chatCreationRepository: ChatCreationRepository by lazy { entryPoint.chatCreationRepository() }
    override val messageRepository: MessageRepository by lazy { entryPoint.messageRepository() }
    override val inlineBotRepository: InlineBotRepository by lazy { entryPoint.inlineBotRepository() }
    override val chatEventLogRepository: ChatEventLogRepository by lazy { entryPoint.chatEventLogRepository() }
    override val messageAiRepository: MessageAiRepository by lazy { entryPoint.messageAiRepository() }
    override val paymentRepository: PaymentRepository by lazy { entryPoint.paymentRepository() }
    override val fileRepository: FileRepository by lazy { entryPoint.fileRepository() }
    override val webAppRepository: WebAppRepository by lazy { entryPoint.webAppRepository() }
    override val userRepository: UserRepository by lazy { entryPoint.userRepository() }
    override val userProfileEditRepository: UserProfileEditRepository by lazy { entryPoint.userProfileEditRepository() }
    override val profilePhotoRepository: ProfilePhotoRepository by lazy { entryPoint.profilePhotoRepository() }
    override val chatInfoRepository: ChatInfoRepository by lazy { entryPoint.chatInfoRepository() }
    override val premiumRepository: PremiumRepository by lazy { entryPoint.premiumRepository() }
    override val botRepository: BotRepository by lazy { entryPoint.botRepository() }
    override val chatStatisticsRepository: ChatStatisticsRepository by lazy { entryPoint.chatStatisticsRepository() }
    override val sponsorRepository: SponsorRepository by lazy { entryPoint.sponsorRepository() }
    override val notificationSettingsRepository: NotificationSettingsRepository by lazy { entryPoint.notificationSettingsRepository() }
    override val sessionRepository: SessionRepository by lazy { entryPoint.sessionRepository() }
    override val wallpaperRepository: WallpaperRepository by lazy { entryPoint.wallpaperRepository() }
    override val storageRepository: StorageRepository by lazy { entryPoint.storageRepository() }
    override val networkStatisticsRepository: NetworkStatisticsRepository by lazy { entryPoint.networkStatisticsRepository() }
    override val attachMenuBotRepository: AttachMenuBotRepository by lazy { entryPoint.attachMenuBotRepository() }
    override val locationRepository: LocationRepository by lazy { entryPoint.locationRepository() }
    override val privacyRepository: PrivacyRepository by lazy { entryPoint.privacyRepository() }
    override val linkHandlerRepository: LinkHandlerRepository by lazy { entryPoint.linkHandlerRepository() }
    override val externalProxyRepository: ExternalProxyRepository by lazy { entryPoint.externalProxyRepository() }
    override val stickerRepository: StickerRepository by lazy { entryPoint.stickerRepository() }
    override val gifRepository: GifRepository by lazy { entryPoint.gifRepository() }
    override val emojiRepository: EmojiRepository by lazy { entryPoint.emojiRepository() }
    override val updateRepository: UpdateRepository by lazy { entryPoint.updateRepository() }
    override val streamingRepository: StreamingRepository by lazy { entryPoint.streamingRepository() }
}

private class HiltUtilsContainer(
    private val entryPoint: AppContainerEntryPoint,
) : UtilsContainer {
    override val appCoroutineScope: CoroutineScope by lazy { entryPoint.appCoroutineScope() }
    override val videoPlayerPool: VideoPlayerPool by lazy { entryPoint.videoPlayerPool() }
    override val exoPlayerCache: ExoPlayerCache by lazy { entryPoint.exoPlayerCache() }
    override val cacheController: CacheController by lazy { entryPoint.cacheController() }
    override val imageLoader: ImageLoader by lazy { entryPoint.imageLoader() }
    override val clipManager: ClipManager by lazy { entryPoint.clipManager() }
    override val dispatcherProvider: DispatcherProvider by lazy { entryPoint.dispatcherProvider() }
    override val logger: Logger by lazy { entryPoint.logger() }

    override fun messageDisplayer(): MessageDisplayer = entryPoint.messageDisplayer()
    override fun externalNavigator(): ExternalNavigator = entryPoint.externalNavigator()
    override fun phoneManager(): PhoneManager = entryPoint.phoneManager()
    override fun domainManager(): DomainManager = entryPoint.domainManager()
    override fun assetsManager(): AssetsManager = entryPoint.assetsManager()
    override fun distrManager(): DistrManager = entryPoint.distrManager()
    override fun downloadUtils(): IDownloadUtils = entryPoint.downloadUtils()
    override fun stringProvider(): StringProvider = entryPoint.stringProvider()
    override fun playerDataSourceFactory(): PlayerDataSourceFactory = entryPoint.playerDataSourceFactory()
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppContainerEntryPoint {
    fun appPreferences(): AppPreferences
    fun appPreferencesProvider(): AppPreferencesProvider
    fun botPreferencesProvider(): BotPreferencesProvider
    fun editorSnippetProvider(): EditorSnippetProvider
    fun cacheProvider(): CacheProvider
    fun authRepository(): AuthRepository
    fun chatListRepository(): ChatListRepository
    fun chatFolderRepository(): ChatFolderRepository
    fun chatOperationsRepository(): ChatOperationsRepository
    fun chatSearchRepository(): ChatSearchRepository
    fun forumTopicsRepository(): ForumTopicsRepository
    fun chatSettingsRepository(): ChatSettingsRepository
    fun chatCreationRepository(): ChatCreationRepository
    fun messageRepository(): MessageRepository
    fun inlineBotRepository(): InlineBotRepository
    fun chatEventLogRepository(): ChatEventLogRepository
    fun messageAiRepository(): MessageAiRepository
    fun paymentRepository(): PaymentRepository
    fun fileRepository(): FileRepository
    fun webAppRepository(): WebAppRepository
    fun userRepository(): UserRepository
    fun userProfileEditRepository(): UserProfileEditRepository
    fun profilePhotoRepository(): ProfilePhotoRepository
    fun chatInfoRepository(): ChatInfoRepository
    fun premiumRepository(): PremiumRepository
    fun botRepository(): BotRepository
    fun chatStatisticsRepository(): ChatStatisticsRepository
    fun sponsorRepository(): SponsorRepository
    fun notificationSettingsRepository(): NotificationSettingsRepository
    fun sessionRepository(): SessionRepository
    fun wallpaperRepository(): WallpaperRepository
    fun storageRepository(): StorageRepository
    fun networkStatisticsRepository(): NetworkStatisticsRepository
    fun attachMenuBotRepository(): AttachMenuBotRepository
    fun locationRepository(): LocationRepository
    fun privacyRepository(): PrivacyRepository
    fun linkHandlerRepository(): LinkHandlerRepository
    fun externalProxyRepository(): ExternalProxyRepository
    fun stickerRepository(): StickerRepository
    fun gifRepository(): GifRepository
    fun emojiRepository(): EmojiRepository
    fun updateRepository(): UpdateRepository
    fun streamingRepository(): StreamingRepository
    fun appCoroutineScope(): CoroutineScope
    fun videoPlayerPool(): VideoPlayerPool
    fun exoPlayerCache(): ExoPlayerCache
    fun cacheController(): CacheController
    fun imageLoader(): ImageLoader
    fun clipManager(): ClipManager
    fun dispatcherProvider(): DispatcherProvider
    fun logger(): Logger
    fun messageDisplayer(): MessageDisplayer
    fun externalNavigator(): ExternalNavigator
    fun phoneManager(): PhoneManager
    fun domainManager(): DomainManager
    fun assetsManager(): AssetsManager
    fun distrManager(): DistrManager
    fun downloadUtils(): IDownloadUtils
    fun stringProvider(): StringProvider
    fun playerDataSourceFactory(): PlayerDataSourceFactory
}
