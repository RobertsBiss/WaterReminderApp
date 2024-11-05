package com.example.waterreminder.util

import android.content.Context
import androidx.work.*
import com.example.waterreminder.worker.WaterReminderWorker
import java.util.concurrent.TimeUnit

class ReminderManager(private val context: Context) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleReminder(intervalSeconds: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val reminderRequest = OneTimeWorkRequestBuilder<WaterReminderWorker>()
            .setInitialDelay(intervalSeconds.toLong(), TimeUnit.SECONDS) // Initial delay set here
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            WaterReminderWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE, // Replace if there's already one running
            reminderRequest
        )
    }


    fun cancelReminders() {
        workManager.cancelUniqueWork(WaterReminderWorker.WORK_NAME)
    }
}