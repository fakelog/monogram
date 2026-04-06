package org.monogram.presentation.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import org.monogram.domain.repository.*
import org.monogram.presentation.core.util.AppPreferences
import kotlin.reflect.KClass

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer is not provided")
}

@Composable
inline fun <reified T : Any> appInject(): T {
    val container = LocalAppContainer.current
    return remember(container) {
        resolveAppDependency(container, T::class)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> resolveAppDependency(container: AppContainer, type: KClass<T>): T {
    return when (type) {
        AppPreferences::class -> container.preferences.appPreferences
        BotPreferencesProvider::class -> container.preferences.botPreferencesProvider
        EditorSnippetProvider::class -> container.preferences.editorSnippetProvider
        EmojiRepository::class -> container.repositories.emojiRepository
        FileRepository::class -> container.repositories.fileRepository
        GifRepository::class -> container.repositories.gifRepository
        LocationRepository::class -> container.repositories.locationRepository
        MessageAiRepository::class -> container.repositories.messageAiRepository
        MessageRepository::class -> container.repositories.messageRepository
        PaymentRepository::class -> container.repositories.paymentRepository
        PlayerDataSourceFactory::class -> container.utils.playerDataSourceFactory()
        StickerRepository::class -> container.repositories.stickerRepository
        StreamingRepository::class -> container.repositories.streamingRepository
        UserRepository::class -> container.repositories.userRepository
        WebAppRepository::class -> container.repositories.webAppRepository
        else -> error("Unsupported app dependency: ${type.qualifiedName}")
    } as T
}
