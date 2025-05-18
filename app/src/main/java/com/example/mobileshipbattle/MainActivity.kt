package com.example.mobileshipbattle

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        if (prefs.getBoolean("music_enabled", false)) {
            startService(Intent(this, MusicService::class.java))
        }


        // Game start
        findViewById<Button>(R.id.start_button).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        findViewById<Button>(R.id.how_to_play_button).setOnClickListener {
            startActivity(Intent(this, HowToPlayActivity2::class.java))
        }

        findViewById<Button>(R.id.settings_button).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.exit_button).setOnClickListener {
            stopService(Intent(this, MusicService::class.java))
            finishAffinity()
        }


        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }


    // Language
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_language", "en") ?: "en"
        val locale = Locale(lang)
        val config = Configuration()
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.lang_en -> {
                changeLanguage("en")
                return true
            }
            R.id.lang_hr -> {
                changeLanguage("hr")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeLanguage(lang: String) {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
        prefs.putString("app_language", lang)
        prefs.apply()

        val intent = intent
        finish()
        startActivity(intent)
    }
}