package org.monogram.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import org.monogram.data.di.TdNotificationManager
import org.monogram.data.gateway.TelegramGateway
import org.monogram.domain.repository.StringProvider
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReplyReceiver : BroadcastReceiver() {

    @Inject
    lateinit var gateway: TelegramGateway

    @Inject
    lateinit var notificationManager: TdNotificationManager

    @Inject
    lateinit var stringProvider: StringProvider

    override fun onReceive(context: Context, intent: Intent) {
        val chatId = intent.getLongExtra("chat_id", 0L)
        val notificationId = intent.getIntExtra("notification_id", 0)
        if (chatId == 0L) return

        val remoteInput = RemoteInput.getResultsFromIntent(intent) ?: return
        val replyText = remoteInput.getCharSequence(TdNotificationManager.KEY_TEXT_REPLY)?.toString() ?: return

        goAsync {
            try {
                val actionTyping = TdApi.SendChatAction().apply {
                    this.chatId = chatId
                    this.topicId = null
                    this.action = TdApi.ChatActionTyping()
                }

                launch {
                    runCatching { gateway.execute(actionTyping) }
                }

                val chat = gateway.execute(TdApi.GetChat(chatId))

                val inputMessageContent = TdApi.InputMessageText().apply {
                    this.text = TdApi.FormattedText(replyText, emptyArray())
                    this.clearDraft = true
                }

                val request = TdApi.SendMessage().apply {
                    this.chatId = chatId
                    this.replyTo = TdApi.InputMessageReplyToMessage()
                    this.options = TdApi.MessageSendOptions().apply {
                        this.disableNotification = false
                        this.fromBackground = true
                    }
                    this.inputMessageContent = inputMessageContent
                }

                gateway.execute(request)

                if (notificationId != 0) {
                    notificationManager.removeNotification(chatId, notificationId)
                }

                notificationManager.appendMessageToNotification(
                    chatId = chatId,
                    messageId = System.currentTimeMillis(),
                    chatType = chat.type,
                    senderName = stringProvider.getString("notification_person_me"),
                    senderBitmap = null,
                    chatIcon = null,
                    text = replyText,
                    timestamp = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
