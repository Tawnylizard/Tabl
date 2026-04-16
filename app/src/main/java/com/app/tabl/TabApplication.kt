package com.app.tabl

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TabApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channels = listOf(
            NotificationChannel(
                CHANNEL_REMINDERS,
                "Напоминания о лекарствах",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о времени приёма лекарств"
                enableVibration(true)
            },
            NotificationChannel(
                CHANNEL_STOCK,
                "Пополнение запасов",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления о низком запасе таблеток"
            }
        )
        manager.createNotificationChannels(channels)
    }

    companion object {
        const val CHANNEL_REMINDERS = "tabl_reminders"
        const val CHANNEL_STOCK = "tabl_stock"
    }
}
