package com.example.mobileshipbattle

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.mobileshipbattle.databinding.ActivityHowToPlay2Binding
import java.util.*

class HowToPlayActivity2 : AppCompatActivity() {

    lateinit var binding: ActivityHowToPlay2Binding

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
        binding = ActivityHowToPlay2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
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
        val prefs = getSharedPreferences("settings", MODE_PRIVATE).edit()
        prefs.putString("app_language", lang)
        prefs.apply()

        val intent = intent
        finish()
        startActivity(intent)
    }
}
