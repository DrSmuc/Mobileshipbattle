package com.example.mobileshipbattle

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.mobileshipbattle.databinding.SettingsBinding
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsBinding

    override fun attachBaseContext(newBase: Context?) {
        val lang = newBase?.getSharedPreferences("settings", Context.MODE_PRIVATE)
            ?.getString("app_language", "en") ?: "en"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        super.attachBaseContext(newBase?.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        // Restore switch state
        binding.switchMusic.isChecked = prefs.getBoolean("music_enabled", false)
        binding.switchSound.isChecked = prefs.getBoolean("sound_enabled", true)

        binding.backSettings.setOnClickListener {
            finish()
        }

        // Sound toggle
        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sound_enabled", isChecked).apply()
            Toast.makeText(this, "Sound ${if (isChecked) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
        }

        // Music toggle
        binding.switchMusic.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("music_enabled", isChecked).apply()
            val intent = Intent(this, MusicService::class.java)
            if (isChecked) {
                startService(intent)
                Toast.makeText(this, "Music ON", Toast.LENGTH_SHORT).show()
            } else {
                stopService(intent)
                Toast.makeText(this, "Music OFF", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
