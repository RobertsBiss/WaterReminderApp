package com.example.waterreminder

import android.content.Context


object PrefsHelper {
    private const val PREFS_NAME = "my_prefs"
    private const val IS_FIRST_LAUNCH = "is_first_launch"

    fun isFirstLaunch(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(IS_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(IS_FIRST_LAUNCH, false).apply()
    }
}