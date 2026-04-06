package org.monogram.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import org.monogram.data.di.TdNotificationManager
import javax.inject.Inject

@AndroidEntryPoint
class NotificationDismissReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: TdNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        val chatId = intent.getLongExtra("chat_id", 0L)
        val notificationId = intent.getIntExtra("notification_id", 0)
        if (chatId != 0L) {
            if (notificationId != 0) {
                notificationManager.removeNotification(chatId, notificationId)
            } else {
                notificationManager.clearHistory(chatId)
            }
        }
    }
}
