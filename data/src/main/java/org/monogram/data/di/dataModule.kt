package org.monogram.data.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.monogram.core.DispatcherProvider
import org.monogram.core.date.DateFormatManager
import org.monogram.data.chats.ChatCache
import org.monogram.data.datasource.FileDataSource
import org.monogram.data.datasource.PlayerDataSourceFactoryImpl
import org.monogram.data.datasource.TdFileDataSource
import org.monogram.data.datasource.cache.*
import org.monogram.data.datasource.remote.*
import org.monogram.data.db.dao.*
import org.monogram.data.db.MonogramDatabase
import org.monogram.data.db.MonogramMigrations
import org.monogram.data.gateway.TelegramGateway
import org.monogram.data.gateway.TelegramGatewayImpl
import org.monogram.data.gateway.UpdateDispatcher
import org.monogram.data.gateway.UpdateDispatcherImpl
import org.monogram.data.infra.*
import org.monogram.data.mapper.*
import org.monogram.data.mapper.message.MessageContentMapper
import org.monogram.data.mapper.message.MessagePersistenceMapper
import org.monogram.data.mapper.message.MessageSenderResolver
import org.monogram.data.repository.*
import org.monogram.data.repository.user.UserRepositoryImpl
import org.monogram.data.stickers.StickerFileManager
import org.monogram.domain.repository.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideCoroutineScope(dispatcherProvider: DispatcherProvider): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatcherProvider.default)

    @Provides
    @Singleton
    internal fun provideTdLibClient() = TdLibClient()

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideStringProvider(@ApplicationContext context: Context): StringProvider =
        AndroidStringProvider(context)

    @Provides
    @Singleton
    fun provideTdLibParametersProvider(@ApplicationContext context: Context) =
        TdLibParametersProvider(context)

    @Provides
    @Singleton
    fun provideChatCache() = ChatCache()

    @Provides
    @Singleton
    internal fun provideTelegramGateway(tdLibClient: TdLibClient): TelegramGateway =
        TelegramGatewayImpl(tdLibClient)

    @Provides
    @Singleton
    fun provideUpdateDispatcher(gateway: TelegramGateway): UpdateDispatcher =
        UpdateDispatcherImpl(gateway = gateway)

    @Provides
    @Singleton
    fun provideFileDataSource(
        gateway: TelegramGateway,
        fileDownloadQueue: FileDownloadQueue,
    ): FileDataSource = TdFileDataSource(
        gateway = gateway,
        fileDownloadQueue = fileDownloadQueue,
    )

    @Provides
    fun provideAuthRemoteDataSource(gateway: TelegramGateway): AuthRemoteDataSource =
        TdAuthRemoteDataSource(gateway = gateway)

    @Provides
    @Singleton
    fun provideNominatimRemoteDataSource() = NominatimRemoteDataSource()

    @Provides
    fun providePlayerDataSourceFactory(fileDataSource: FileDataSource): PlayerDataSourceFactory =
        PlayerDataSourceFactoryImpl(fileDataSource = fileDataSource)

    @Provides
    @Singleton
    fun provideAuthRepository(
        parametersProvider: TdLibParametersProvider,
        remote: AuthRemoteDataSource,
        updates: UpdateDispatcher,
        scope: CoroutineScope,
    ): AuthRepository = AuthRepositoryImpl(
        parametersProvider = parametersProvider,
        remote = remote,
        updates = updates,
        scope = scope,
    )

    @Provides
    fun provideUserRemoteDataSource(gateway: TelegramGateway): UserRemoteDataSource =
        TdUserRemoteDataSource(gateway = gateway)

    @Provides
    fun provideLinkRemoteDataSource(gateway: TelegramGateway): LinkRemoteDataSource =
        TdLinkRemoteDataSource(gateway = gateway)

    @Provides
    @Singleton
    fun provideMonogramDatabase(@ApplicationContext context: Context): MonogramDatabase {
        return Room.databaseBuilder(
            context,
            MonogramDatabase::class.java,
            "monogram_db",
        )
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .addMigrations(MonogramMigrations.MIGRATION_26_27)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideChatDao(database: MonogramDatabase) = database.chatDao()

    @Provides
    @Singleton
    fun provideMessageDao(database: MonogramDatabase) = database.messageDao()

    @Provides
    @Singleton
    fun provideUserDao(database: MonogramDatabase) = database.userDao()

    @Provides
    @Singleton
    fun provideChatFullInfoDao(database: MonogramDatabase) = database.chatFullInfoDao()

    @Provides
    @Singleton
    fun provideTopicDao(database: MonogramDatabase) = database.topicDao()

    @Provides
    @Singleton
    fun provideUserFullInfoDao(database: MonogramDatabase) = database.userFullInfoDao()

    @Provides
    @Singleton
    fun provideStickerSetDao(database: MonogramDatabase) = database.stickerSetDao()

    @Provides
    @Singleton
    fun provideRecentEmojiDao(database: MonogramDatabase) = database.recentEmojiDao()

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: MonogramDatabase) = database.searchHistoryDao()

    @Provides
    @Singleton
    fun provideChatFolderDao(database: MonogramDatabase) = database.chatFolderDao()

    @Provides
    @Singleton
    fun provideAttachBotDao(database: MonogramDatabase) = database.attachBotDao()

    @Provides
    @Singleton
    fun provideKeyValueDao(database: MonogramDatabase) = database.keyValueDao()

    @Provides
    @Singleton
    fun provideNotificationExceptionDao(database: MonogramDatabase) = database.notificationExceptionDao()

    @Provides
    @Singleton
    fun provideNotificationSettingDao(database: MonogramDatabase) = database.notificationSettingDao()

    @Provides
    @Singleton
    fun provideWallpaperDao(database: MonogramDatabase) = database.wallpaperDao()

    @Provides
    @Singleton
    fun provideStickerPathDao(database: MonogramDatabase) = database.stickerPathDao()

    @Provides
    @Singleton
    fun provideSponsorDao(database: MonogramDatabase) = database.sponsorDao()

    @Provides
    @Singleton
    fun provideTextCompositionStyleDao(database: MonogramDatabase) = database.textCompositionStyleDao()

    @Provides
    @Singleton
    fun provideUserLocalDataSource(
        userDao: UserDao,
        userFullInfoDao: UserFullInfoDao,
    ): UserLocalDataSource = RoomUserLocalDataSource(
        userDao = userDao,
        userFullInfoDao = userFullInfoDao,
    )

    @Provides
    @Singleton
    fun provideChatLocalDataSource(
        database: MonogramDatabase,
        chatDao: ChatDao,
        messageDao: MessageDao,
        chatFullInfoDao: ChatFullInfoDao,
        topicDao: TopicDao,
    ): ChatLocalDataSource = RoomChatLocalDataSource(
        database = database,
        chatDao = chatDao,
        messageDao = messageDao,
        chatFullInfoDao = chatFullInfoDao,
        topicDao = topicDao,
    )

    @Provides
    @Singleton
    fun provideStickerLocalDataSource(
        stickerSetDao: StickerSetDao,
        recentEmojiDao: RecentEmojiDao,
        stickerPathDao: StickerPathDao,
    ): StickerLocalDataSource = RoomStickerLocalDataSource(
        stickerSetDao = stickerSetDao,
        recentEmojiDao = recentEmojiDao,
        stickerPathDao = stickerPathDao,
    )

    @Provides
    @Singleton
    fun provideUserRepository(
        remote: UserRemoteDataSource,
        userLocal: UserLocalDataSource,
        chatLocal: ChatLocalDataSource,
        chatCache: ChatCache,
        updates: UpdateDispatcher,
        scope: CoroutineScope,
        gateway: TelegramGateway,
        fileQueue: FileDownloadQueue,
        fileObserverHub: FileObserverHub,
        keyValueDao: KeyValueDao,
        cacheProvider: CacheProvider,
    ): UserRepository = UserRepositoryImpl(
        remote = remote,
        userLocal = userLocal,
        chatLocal = chatLocal,
        chatCache = chatCache,
        updates = updates,
        scope = scope,
        gateway = gateway,
        fileQueue = fileQueue,
        fileObserverHub = fileObserverHub,
        keyValueDao = keyValueDao,
        cacheProvider = cacheProvider,
    )

    @Provides
    @Singleton
    fun provideUserProfileEditRepository(remote: UserRemoteDataSource): UserProfileEditRepository =
        UserProfileEditRepositoryImpl(remote = remote)

    @Provides
    @Singleton
    fun provideProfilePhotoRepository(
        remote: UserRemoteDataSource,
        chatLocal: ChatLocalDataSource,
        gateway: TelegramGateway,
        fileQueue: FileDownloadQueue,
        fileObserverHub: FileObserverHub
    ): ProfilePhotoRepository = ProfilePhotoRepositoryImpl(
        remote = remote,
        chatLocal = chatLocal,
        gateway = gateway,
        fileQueue = fileQueue,
        fileObserverHub = fileObserverHub
    )

    @Provides
    @Singleton
    fun provideChatInfoRepository(
        remote: UserRemoteDataSource,
        chatLocal: ChatLocalDataSource,
        userRepository: UserRepository,
    ): ChatInfoRepository = ChatInfoRepositoryImpl(
        remote = remote,
        chatLocal = chatLocal,
        userRepository = userRepository,
    )

    @Provides
    @Singleton
    fun providePremiumRepository(remote: UserRemoteDataSource): PremiumRepository =
        PremiumRepositoryImpl(remote = remote)

    @Provides
    @Singleton
    fun provideBotRepository(remote: UserRemoteDataSource): BotRepository =
        BotRepositoryImpl(remote = remote)

    @Provides
    @Singleton
    fun provideChatStatisticsRepository(remote: UserRemoteDataSource): ChatStatisticsRepository =
        ChatStatisticsRepositoryImpl(remote = remote)

    @Provides
    @Singleton
    fun provideChatsRemoteDataSource(gateway: TelegramGateway): ChatsRemoteDataSource =
        TdChatsRemoteDataSource(gateway = gateway)

    @Provides
    @Singleton
    fun provideChatsCacheDataSource(chatCache: ChatCache): ChatsCacheDataSource = chatCache

    @Provides
    @Singleton
    fun provideChatRemoteSource(
        @ApplicationContext context: Context,
        gateway: TelegramGateway,
    ): ChatRemoteSource = TdChatRemoteSource(
        gateway = gateway,
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager,
    )

    @Provides
    fun provideProxyRemoteDataSource(gateway: TelegramGateway): ProxyRemoteDataSource =
        TdProxyRemoteDataSource(gateway = gateway)

    @Provides
    @Singleton
    fun provideChatMapper(
        stringProvider: StringProvider,
        dateFormatManager: DateFormatManager
    ) = ChatMapper(stringProvider, dateFormatManager)

    @Provides
    @Singleton
    fun provideStorageMapper(stringProvider: StringProvider) = StorageMapper(stringProvider)

    @Provides
    @Singleton
    fun provideNetworkMapper(
        stringProvider: StringProvider,
        storageMapper: StorageMapper,
    ) = NetworkMapper(stringProvider, storageMapper)

    @Provides
    @Singleton
    fun provideMessageFileApi(fileDownloadQueue: FileDownloadQueue): MessageFileApi =
        MessageFileCoordinator(fileDownloadQueue = fileDownloadQueue)

    @Provides
    @Singleton
    fun provideUserCacheDataSource(chatCache: ChatCache): UserCacheDataSource = chatCache

    @Provides
    @Singleton
    fun provideTdFileHelper(
        @ApplicationContext context: Context,
        fileApi: MessageFileApi,
        appPreferences: AppPreferencesProvider,
        chatCache: ChatCache,
    ) = TdFileHelper(
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager,
        fileApi = fileApi,
        appPreferences = appPreferences,
        cache = chatCache,
    )

    @Provides
    @Singleton
    internal fun provideCustomEmojiLoader(
        gateway: TelegramGateway,
        fileApi: MessageFileApi,
        fileUpdateHandler: FileUpdateHandler,
        fileHelper: TdFileHelper,
    ) = CustomEmojiLoader(
        gateway = gateway,
        fileApi = fileApi,
        fileUpdateHandler = fileUpdateHandler,
        fileHelper = fileHelper,
    )

    @Provides
    @Singleton
    internal fun provideWebPageMapper(
        fileHelper: TdFileHelper,
        appPreferences: AppPreferencesProvider,
    ) = WebPageMapper(
        fileHelper = fileHelper,
        appPreferences = appPreferences,
    )

    @Provides
    @Singleton
    internal fun provideMessageContentMapper(
        fileHelper: TdFileHelper,
        appPreferences: AppPreferencesProvider,
        customEmojiLoader: CustomEmojiLoader,
        webPageMapper: WebPageMapper,
        scope: CoroutineScope,
    ) = MessageContentMapper(
        fileHelper = fileHelper,
        appPreferences = appPreferences,
        customEmojiLoader = customEmojiLoader,
        webPageMapper = webPageMapper,
        scope = scope,
    )

    @Provides
    @Singleton
    internal fun provideMessageSenderResolver(
        gateway: TelegramGateway,
        userRepository: UserRepository,
        chatInfoRepository: ChatInfoRepository,
        cache: ChatCache,
        fileHelper: TdFileHelper,
    ) = MessageSenderResolver(
        gateway = gateway,
        userRepository = userRepository,
        chatInfoRepository = chatInfoRepository,
        cache = cache,
        fileHelper = fileHelper,
    )

    @Provides
    @Singleton
    internal fun provideMessagePersistenceMapper(
        cache: ChatCache,
        fileHelper: TdFileHelper,
    ) = MessagePersistenceMapper(
        cache = cache,
        fileHelper = fileHelper,
    )

    @Provides
    @Singleton
    internal fun provideMessageMapper(
        gateway: TelegramGateway,
        userRepository: UserRepository,
        cache: ChatCache,
        fileHelper: TdFileHelper,
        senderResolver: MessageSenderResolver,
        contentMapper: MessageContentMapper,
        persistenceMapper: MessagePersistenceMapper,
        customEmojiLoader: CustomEmojiLoader,
    ) = MessageMapper(
        gateway = gateway,
        userRepository = userRepository,
        cache = cache,
        fileHelper = fileHelper,
        senderResolver = senderResolver,
        contentMapper = contentMapper,
        persistenceMapper = persistenceMapper,
        customEmojiLoader = customEmojiLoader,
    )

    @Provides
    @Singleton
    fun provideConnectionManager(
        @ApplicationContext context: Context,
        chatRemoteSource: ChatRemoteSource,
        proxyRemoteSource: ProxyRemoteDataSource,
        updates: UpdateDispatcher,
        appPreferences: AppPreferencesProvider,
        dispatchers: DispatcherProvider,
        scope: CoroutineScope,
    ) = ConnectionManager(
        chatRemoteSource = chatRemoteSource,
        proxyRemoteSource = proxyRemoteSource,
        updates = updates,
        appPreferences = appPreferences,
        dispatchers = dispatchers,
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager,
        scope = scope,
    )

    @Provides
    @Singleton
    fun provideChatsListRepositoryImpl(
        @ApplicationContext context: Context,
        remoteDataSource: ChatsRemoteDataSource,
        chatRemoteSource: ChatRemoteSource,
        updates: UpdateDispatcher,
        appPreferences: AppPreferencesProvider,
        cacheProvider: CacheProvider,
        dispatchers: DispatcherProvider,
        cache: ChatCache,
        chatMapper: ChatMapper,
        messageMapper: MessageMapper,
        gateway: TelegramGateway,
        scope: CoroutineScope,
        chatLocalDataSource: ChatLocalDataSource,
        connectionManager: ConnectionManager,
        searchHistoryDao: SearchHistoryDao,
        chatFolderDao: ChatFolderDao,
        userFullInfoDao: UserFullInfoDao,
        fileQueue: FileDownloadQueue,
        fileUpdateHandler: FileUpdateHandler,
        stringProvider: StringProvider,
    ) = ChatsListRepositoryImpl(
        remoteDataSource = remoteDataSource,
        chatRemoteSource = chatRemoteSource,
        updates = updates,
        appPreferences = appPreferences,
        cacheProvider = cacheProvider,
        dispatchers = dispatchers,
        cache = cache,
        chatMapper = chatMapper,
        messageMapper = messageMapper,
        gateway = gateway,
        scope = scope,
        chatLocalDataSource = chatLocalDataSource,
        connectionManager = connectionManager,
        databaseFile = context.getDatabasePath("monogram_db"),
        searchHistoryDao = searchHistoryDao,
        chatFolderDao = chatFolderDao,
        userFullInfoDao = userFullInfoDao,
        fileQueue = fileQueue,
        fileUpdateHandler = fileUpdateHandler,
        stringProvider = stringProvider,
    )

    @Provides
    @Singleton
    fun provideChatListRepository(repository: ChatsListRepositoryImpl): ChatListRepository = repository

    @Provides
    @Singleton
    fun provideChatFolderRepository(repository: ChatsListRepositoryImpl): ChatFolderRepository = repository

    @Provides
    @Singleton
    fun provideChatOperationsRepository(repository: ChatsListRepositoryImpl): ChatOperationsRepository = repository

    @Provides
    @Singleton
    fun provideChatSearchRepository(repository: ChatsListRepositoryImpl): ChatSearchRepository = repository

    @Provides
    @Singleton
    fun provideForumTopicsRepository(repository: ChatsListRepositoryImpl): ForumTopicsRepository = repository

    @Provides
    @Singleton
    fun provideChatSettingsRepository(repository: ChatsListRepositoryImpl): ChatSettingsRepository = repository

    @Provides
    @Singleton
    fun provideChatCreationRepository(repository: ChatsListRepositoryImpl): ChatCreationRepository = repository

    @Provides
    fun provideSettingsRemoteDataSource(
        gateway: TelegramGateway,
        fileQueue: FileDownloadQueue,
    ): SettingsRemoteDataSource = TdSettingsRemoteDataSource(
        gateway = gateway,
        fileQueue = fileQueue,
    )

    @Provides
    @Singleton
    fun provideSettingsCacheDataSource(): SettingsCacheDataSource = InMemorySettingsCacheDataSource()

    @Provides
    @Singleton
    fun provideNotificationSettingsRepository(
        remote: SettingsRemoteDataSource,
        cache: SettingsCacheDataSource,
        chatsRemote: ChatsRemoteDataSource,
        notificationExceptionDao: NotificationExceptionDao,
        updates: UpdateDispatcher,
        scope: CoroutineScope,
        dispatchers: DispatcherProvider,
    ): NotificationSettingsRepository = NotificationSettingsRepositoryImpl(
        remote = remote,
        cache = cache,
        chatsRemote = chatsRemote,
        notificationExceptionDao = notificationExceptionDao,
        updates = updates,
        scope = scope,
        dispatchers = dispatchers,
    )

    @Provides
    @Singleton
    fun provideSessionRepository(remote: SettingsRemoteDataSource): SessionRepository =
        SessionRepositoryImpl(remote = remote)

    @Provides
    @Singleton
    fun provideWallpaperRepository(
        remote: SettingsRemoteDataSource,
        wallpaperDao: WallpaperDao,
        fileObserverHub: FileObserverHub,
        dispatchers: DispatcherProvider,
        scope: CoroutineScope,
    ): WallpaperRepository = WallpaperRepositoryImpl(
        remote = remote,
        wallpaperDao = wallpaperDao,
        fileObserverHub = fileObserverHub,
        dispatchers = dispatchers,
        scope = scope,
    )

    @Provides
    @Singleton
    fun provideStorageRepository(
        remote: SettingsRemoteDataSource,
        cache: SettingsCacheDataSource,
        chatsRemote: ChatsRemoteDataSource,
        dispatchers: DispatcherProvider,
        storageMapper: StorageMapper,
        stringProvider: StringProvider,
    ): StorageRepository = StorageRepositoryImpl(
        remote = remote,
        cache = cache,
        chatsRemote = chatsRemote,
        dispatchers = dispatchers,
        storageMapper = storageMapper,
        stringProvider = stringProvider,
    )

    @Provides
    @Singleton
    fun provideNetworkStatisticsRepository(
        remote: SettingsRemoteDataSource,
        networkMapper: NetworkMapper,
    ): NetworkStatisticsRepository = NetworkStatisticsRepositoryImpl(
        remote = remote,
        networkMapper = networkMapper,
    )

    @Provides
    @Singleton
    fun provideAttachMenuBotRepository(
        remote: SettingsRemoteDataSource,
        cache: SettingsCacheDataSource,
        cacheProvider: CacheProvider,
        updates: UpdateDispatcher,
        fileObserverHub: FileObserverHub,
        dispatchers: DispatcherProvider,
        attachBotDao: AttachBotDao,
        scope: CoroutineScope,
    ): AttachMenuBotRepository = AttachMenuBotRepositoryImpl(
        remote = remote,
        cache = cache,
        cacheProvider = cacheProvider,
        updates = updates,
        fileObserverHub = fileObserverHub,
        dispatchers = dispatchers,
        attachBotDao = attachBotDao,
        scope = scope,
    )

    @Provides
    @Singleton
    fun providePollRepository(): PollRepository = PollRepositoryImpl()

    @Provides
    @Singleton
    fun provideMessageRemoteDataSource(
        gateway: TelegramGateway,
        messageMapper: MessageMapper,
        userRepository: UserRepository,
        chatListRepository: ChatListRepository,
        cache: ChatCache,
        pollRepository: PollRepository,
        fileDownloadQueue: FileDownloadQueue,
        fileUpdateHandler: FileUpdateHandler,
        dispatcherProvider: DispatcherProvider,
        scope: CoroutineScope,
    ): MessageRemoteDataSource = TdMessageRemoteDataSource(
        gateway = gateway,
        messageMapper = messageMapper,
        userRepository = userRepository,
        chatListRepository = chatListRepository,
        cache = cache,
        pollRepository = pollRepository,
        fileDownloadQueue = fileDownloadQueue,
        fileUpdateHandler = fileUpdateHandler,
        dispatcherProvider = dispatcherProvider,
        scope = scope,
    )

    @Provides
    @Singleton
    fun provideMessageRepository(
        @ApplicationContext context: Context,
        gateway: TelegramGateway,
        updates: UpdateDispatcher,
        messageMapper: MessageMapper,
        messageRemoteDataSource: MessageRemoteDataSource,
        cache: ChatCache,
        fileHelper: TdFileHelper,
        dispatcherProvider: DispatcherProvider,
        scope: CoroutineScope,
        fileDataSource: FileDataSource,
        chatLocalDataSource: ChatLocalDataSource,
        userLocalDataSource: UserLocalDataSource,
        stickerPathDao: StickerPathDao,
        keyValueDao: KeyValueDao,
        textCompositionStyleDao: TextCompositionStyleDao,
    ): MessageRepository = MessageRepositoryImpl(
        context = context,
        gateway = gateway,
        updates = updates,
        messageMapper = messageMapper,
        messageRemoteDataSource = messageRemoteDataSource,
        cache = cache,
        fileHelper = fileHelper,
        fileDataSource = fileDataSource,
        dispatcherProvider = dispatcherProvider,
        scope = scope,
        chatLocalDataSource = chatLocalDataSource,
        userLocalDataSource = userLocalDataSource,
        stickerPathDao = stickerPathDao,
        keyValueDao = keyValueDao,
        textCompositionStyleDao = textCompositionStyleDao,
    )

    @Provides
    @Singleton
    fun provideInlineBotRepository(repository: MessageRepository): InlineBotRepository = repository

    @Provides
    @Singleton
    fun provideChatEventLogRepository(repository: MessageRepository): ChatEventLogRepository = repository

    @Provides
    @Singleton
    fun provideMessageAiRepository(repository: MessageRepository): MessageAiRepository = repository

    @Provides
    @Singleton
    fun providePaymentRepository(repository: MessageRepository): PaymentRepository = repository

    @Provides
    @Singleton
    fun provideFileRepository(repository: MessageRepository): FileRepository = repository

    @Provides
    @Singleton
    fun provideWebAppRepository(repository: MessageRepository): WebAppRepository = repository

    @Provides
    fun provideStickerRemoteSource(gateway: TelegramGateway): StickerRemoteSource =
        TdStickerRemoteSource(gateway = gateway)

    @Provides
    fun provideGifRemoteSource(gateway: TelegramGateway): GifRemoteSource =
        TdGifRemoteSource(gateway = gateway)

    @Provides
    fun provideEmojiRemoteSource(gateway: TelegramGateway): EmojiRemoteSource =
        TdEmojiRemoteSource(gateway = gateway)

    @Provides
    @Singleton
    fun provideFileMessageRegistry() = FileMessageRegistry()

    @Provides
    @Singleton
    fun provideFileDownloadQueue(
        gateway: TelegramGateway,
        registry: FileMessageRegistry,
        cache: ChatCache,
        scope: CoroutineScope,
        dispatcherProvider: DispatcherProvider,
    ) = FileDownloadQueue(
        gateway = gateway,
        registry = registry,
        cache = cache,
        scope = scope,
        dispatcherProvider = dispatcherProvider,
    )

    @Provides
    @Singleton
    fun provideFileUpdateHandler(
        registry: FileMessageRegistry,
        queue: FileDownloadQueue,
        updates: UpdateDispatcher,
        scope: CoroutineScope,
    ) = FileUpdateHandler(
        registry = registry,
        queue = queue,
        updates = updates,
        scope = scope,
    )

    @Provides
    @Singleton
    fun provideStickerFileManager(
        localDataSource: StickerLocalDataSource,
        fileQueue: FileDownloadQueue,
        fileUpdateHandler: FileUpdateHandler,
        dispatchers: DispatcherProvider,
        scope: CoroutineScope,
    ) = StickerFileManager(
        localDataSource = localDataSource,
        fileQueue = fileQueue,
        fileUpdateHandler = fileUpdateHandler,
        dispatchers = dispatchers,
        scope = scope,
    )

    @Provides
    @Singleton
    fun provideStickerRepository(
        remote: StickerRemoteSource,
        fileManager: StickerFileManager,
        updates: UpdateDispatcher,
        cacheProvider: CacheProvider,
        dispatchers: DispatcherProvider,
        localDataSource: StickerLocalDataSource,
        scope: CoroutineScope,
    ): StickerRepository = StickerRepositoryImpl(
        remote = remote,
        fileManager = fileManager,
        updates = updates,
        cacheProvider = cacheProvider,
        dispatchers = dispatchers,
        localDataSource = localDataSource,
        scope = scope,
    )

    @Provides
    @Singleton
    fun provideGifRepository(
        remote: GifRemoteSource,
        cacheProvider: CacheProvider,
        stickerFileManager: StickerFileManager,
    ): GifRepository = GifRepositoryImpl(
        remote = remote,
        cacheProvider = cacheProvider,
        stickerFileManager = stickerFileManager,
    )

    @Provides
    @Singleton
    fun provideEmojiRepository(
        @ApplicationContext context: Context,
        remote: EmojiRemoteSource,
        localDataSource: StickerLocalDataSource,
        cacheProvider: CacheProvider,
        dispatchers: DispatcherProvider,
        scope: CoroutineScope,
    ): EmojiRepository = EmojiRepositoryImpl(
        remote = remote,
        localDataSource = localDataSource,
        cacheProvider = cacheProvider,
        dispatchers = dispatchers,
        context = context,
        scope = scope,
    )

    @Provides
    fun providePrivacyRemoteDataSource(gateway: TelegramGateway): PrivacyRemoteDataSource =
        TdPrivacyRemoteDataSource(gateway = gateway)

    @Provides
    @Singleton
    fun providePrivacyRepository(
        remote: PrivacyRemoteDataSource,
        updates: UpdateDispatcher,
    ): PrivacyRepository = PrivacyRepositoryImpl(
        remote = remote,
        updates = updates,
    )

    @Provides
    @Singleton
    fun provideLinkParser() = LinkParser()

    @Provides
    @Singleton
    fun provideLinkHandlerRepository(
        parser: LinkParser,
        remote: LinkRemoteDataSource,
        chatListRepository: ChatListRepository,
        chatInfoRepository: ChatInfoRepository,
        fileQueue: FileDownloadQueue,
    ): LinkHandlerRepository = LinkHandlerRepositoryImpl(
        parser,
        remote,
        chatListRepository,
        chatInfoRepository,
        fileQueue,
    )

    @Provides
    @Singleton
    fun provideStreamingRepository(
        fileDataSource: FileDataSource,
        fileObserverHub: FileObserverHub,
    ): StreamingRepository = StreamingRepositoryImpl(
        fileDataSource = fileDataSource,
        fileObserverHub = fileObserverHub
    )

    @Provides
    @Singleton
    fun provideExternalProxyRepository(
        remote: ProxyRemoteDataSource,
        appPreferences: AppPreferencesProvider,
    ): ExternalProxyRepository = ExternalProxyRepositoryImpl(
        remote = remote,
        appPreferences = appPreferences,
    )

    @Provides
    @Singleton
    fun provideLocationRepository(remote: NominatimRemoteDataSource): LocationRepository =
        LocationRepositoryImpl(remote = remote)

    @Provides
    fun provideUpdateRemoteDateSource(gateway: TelegramGateway): UpdateRemoteDateSource =
        TdUpdateRemoteDataSource(gateway = gateway)

    @Provides
    @Singleton
    fun provideUpdateRepository(
        @ApplicationContext context: Context,
        remote: UpdateRemoteDateSource,
        fileQueue: FileDownloadQueue,
        fileUpdateHandler: FileUpdateHandler,
        authRepository: AuthRepository,
        scope: CoroutineScope,
    ): UpdateRepository = UpdateRepositoryImpl(
        context = context,
        remote = remote,
        fileQueue = fileQueue,
        fileUpdateHandler = fileUpdateHandler,
        authRepository = authRepository,
        scope = scope,
    )

    @Provides
    @Singleton
    fun provideOfflineWarmup(
        scope: CoroutineScope,
        dispatchers: DispatcherProvider,
        gateway: TelegramGateway,
        chatDao: ChatDao,
        messageDao: MessageDao,
        userDao: UserDao,
        userFullInfoDao: UserFullInfoDao,
        chatFullInfoDao: ChatFullInfoDao,
        messageMapper: MessageMapper,
        chatCache: ChatCache,
        stickerRepository: StickerRepository,
    ) = OfflineWarmup(
        scope = scope,
        dispatchers = dispatchers,
        gateway = gateway,
        chatDao = chatDao,
        messageDao = messageDao,
        userDao = userDao,
        userFullInfoDao = userFullInfoDao,
        chatFullInfoDao = chatFullInfoDao,
        messageMapper = messageMapper,
        chatCache = chatCache,
        stickerRepository = stickerRepository,
    )

    @Provides
    @Singleton
    fun provideSponsorSyncManager(
        scope: CoroutineScope,
        gateway: TelegramGateway,
        sponsorDao: SponsorDao,
        authRepository: AuthRepository,
    ) = SponsorSyncManager(
        scope = scope,
        gateway = gateway,
        sponsorDao = sponsorDao,
        authRepository = authRepository,
    )

    @Provides
    @Singleton
    fun provideSponsorRepository(sponsorSyncManager: SponsorSyncManager): SponsorRepository =
        SponsorRepositoryImpl(sponsorSyncManager = sponsorSyncManager)

    @Provides
    @Singleton
    fun provideTdNotificationManager(
        @ApplicationContext context: Context,
        gateway: TelegramGateway,
        appPreferences: AppPreferencesProvider,
        notificationSettingsRepository: NotificationSettingsRepository,
        notificationSettingDao: NotificationSettingDao,
        fileQueue: FileDownloadQueue,
        stringProvider: StringProvider
    ) = TdNotificationManager(
        context,
        gateway,
        appPreferences,
        notificationSettingsRepository,
        notificationSettingDao,
        fileQueue,
        stringProvider = stringProvider
    )
}
