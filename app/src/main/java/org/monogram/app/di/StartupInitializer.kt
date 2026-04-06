package org.monogram.app.di

import org.monogram.data.di.TdNotificationManager
import org.monogram.data.gateway.TelegramGateway
import org.monogram.data.infra.OfflineWarmup
import org.monogram.data.infra.SponsorSyncManager
import org.monogram.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartupInitializer @Inject constructor(
    private val offlineWarmup: OfflineWarmup,
    private val sponsorSyncManager: SponsorSyncManager,
    private val telegramGateway: TelegramGateway,
    private val authRepository: AuthRepository,
    private val notificationManager: TdNotificationManager,
) {
    fun initialize() {
        offlineWarmup.hashCode()
        sponsorSyncManager.hashCode()
        telegramGateway.hashCode()
        authRepository.hashCode()
        notificationManager.hashCode()
    }
}
