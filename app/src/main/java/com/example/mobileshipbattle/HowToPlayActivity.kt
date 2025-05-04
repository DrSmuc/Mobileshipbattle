package com.example.mobileshipbattle

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HowToPlayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.howtoplay)

        findViewById<Button>(R.id.back_button).setOnClickListener {
            finish()
        }
    }
}