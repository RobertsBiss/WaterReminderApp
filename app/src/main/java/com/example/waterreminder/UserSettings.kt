package com.example.waterreminder

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val measurementIsMetric: Boolean = true,
    val timeFormat: String = "24",
    val gender: String = "unspecified",
    val dailyGoal: Int = 2000, // default 2L
    val weight: Double = 0.0,
    val language: String = "en",
    val remindersEnabled: Boolean = false,
    val reminderTime: String = "09:00",
    val isDarkTheme: Boolean = false

)

@Dao
interface UserSettingsDao {

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getDirectUserSettings(): UserSettings?

    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getUserSettings(): LiveData<UserSettings>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: UserSettings)

    @Update
    suspend fun update(settings: UserSettings)
}