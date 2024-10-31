package com.example.waterreminder.util

import android.content.Context
import androidx.work.*
import com.example.waterreminder.worker.WaterReminderWorker
import java.util.concurrent.TimeUnit

class ReminderManager(private val context: Context) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleReminder(intervalHours: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val reminderRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            intervalHours.toLong(), TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WaterReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderRequest
        )
    }

    fun cancelReminders() {
        workManager.cancelUniqueWork(WaterReminderWorker.WORK_NAME)
    }
}
