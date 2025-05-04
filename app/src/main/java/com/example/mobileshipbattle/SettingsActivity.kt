package com.example.mobileshipbattle

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        findViewById<Button>(R.id.back_settings).setOnClickListener {
            finish()
        }

        findViewById<Switch>(R.id.switch_sound).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Sound " + if (isChecked) "ON" else "OFF", Toast.LENGTH_SHORT).show()
        }

        findViewById<Switch>(R.id.switch_music).setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Music " + if (isChecked) "ON" else "OFF", Toast.LENGTH_SHORT).show()
        }
    }
}