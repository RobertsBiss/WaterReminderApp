package com.example.waterreminder.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllLogs(): LiveData<List<WaterLog>>

    @Query("""
        SELECT * FROM water_logs 
        WHERE date(timestamp/1000, 'unixepoch', 'localtime') = date('now', 'localtime')
        ORDER BY timestamp DESC
    """)
    fun getTodayLogs(): LiveData<List<WaterLog>>

    @Query("""
        SELECT * FROM water_logs 
        WHERE date(timestamp/1000, 'unixepoch', 'localtime') = :date
        ORDER BY timestamp DESC
    """)
    fun getLogsForDate(date: String): LiveData<List<WaterLog>>

    @Insert
    suspend fun insert(waterLog: WaterLog)

    @Delete
    suspend fun delete(waterLog: WaterLog)

    @Query("""
        SELECT SUM(amount) as total_amount
        FROM water_logs
        WHERE date(timestamp/1000, 'unixepoch', 'localtime') = :date
    """)
    suspend fun getDailyTotal(date: String): Int?

    @Query("""
        SELECT SUM(amount) as total_amount
        FROM water_logs
        WHERE date(timestamp/1000, 'unixepoch', 'localtime') BETWEEN :startDate AND :endDate
        GROUP BY date(timestamp/1000, 'unixepoch', 'localtime')
        ORDER BY timestamp DESC
    """)
    suspend fun getDateRangeIntake(startDate: String, endDate: String): List<Int>
}
