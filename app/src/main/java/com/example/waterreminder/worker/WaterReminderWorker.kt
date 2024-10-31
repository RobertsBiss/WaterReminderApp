package com.example.waterreminder.worker

import com.example.waterreminder.NotificationHelper
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WaterReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showReminder()
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "water_reminder_work"
    }
}
