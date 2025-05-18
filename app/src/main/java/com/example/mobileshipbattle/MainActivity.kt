package com.example.mobileshipbattle

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobileshipbattle.databinding.ActivityHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale





public class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // game start
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
            finishAffinity() // Closes the entire app
        }

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    // language
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE)
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

    // promjeni jezik i restart app
    private fun changeLanguage(lang: String) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE).edit()
        prefs.putString("app_language", lang)
        prefs.apply()

        // reload app i novi jezik
        val intent = intent
        finish()
        startActivity(intent)
    }
}