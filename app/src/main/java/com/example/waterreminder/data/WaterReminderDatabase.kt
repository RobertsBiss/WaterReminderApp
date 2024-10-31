package com.example.waterreminder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.waterreminder.UserSettings
import com.example.waterreminder.UserSettingsDao

@Database(entities = [WaterLog::class, UserSettings::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class WaterReminderDatabase : RoomDatabase() {
    abstract fun waterLogDao(): WaterLogDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: WaterReminderDatabase? = null

        fun getDatabase(context: Context): WaterReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaterReminderDatabase::class.java,
                    "water_reminder_database"
                )
                    .fallbackToDestructiveMigration() // Add this line temporarily for testing
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}