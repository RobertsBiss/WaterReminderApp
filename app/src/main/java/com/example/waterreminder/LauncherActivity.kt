package com.example.waterreminder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PrefsHelper.isFirstLaunch(this)) {
            startActivity(Intent(this, SetupActivity::class.java)) // Start SetupActivity
            PrefsHelper.setFirstLaunchCompleted(this)
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }

        finish()
    }
}