package com.example.waterreminder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.waterreminder.NotificationHelper
import com.example.waterreminder.util.ReminderManager

class WaterReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showReminder()

        // Reschedule the next reminder after showing the current one
        val reminderManager = ReminderManager(context)

        // Get the interval again (you can pass it through WorkerParams if needed)
        val intervalSeconds = 60 // Or fetch it dynamically if needed
        reminderManager.scheduleReminder(intervalSeconds)

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "water_reminder_work"
    }
}
