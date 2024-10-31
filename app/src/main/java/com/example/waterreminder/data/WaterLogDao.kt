package com.example.waterreminder.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllLogs(): LiveData<List<WaterLog>>

    @Query("SELECT * FROM water_logs WHERE date(timestamp/1000, 'unixepoch') = date('now')")
    fun getTodayLogs(): LiveData<List<WaterLog>>

    @Insert
    suspend fun insert(waterLog: WaterLog)

    @Delete
    suspend fun delete(waterLog: WaterLog)
}