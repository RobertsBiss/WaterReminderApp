package com.example.waterreminder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.waterreminder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if it's the first launch
        if (PrefsHelper.isFirstLaunch(this)) {
            // Start SetupActivity
            startActivity(Intent(this, SetupActivity::class.java))

            // Finish MainActivity to prevent going back
            finish()
            return // Exit onCreate() to avoid further execution
        }

        // If not first launch, proceed with normal setup
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up the top navigation with NavController
        binding.topNav?.setupWithNavController(navController)
    }
}