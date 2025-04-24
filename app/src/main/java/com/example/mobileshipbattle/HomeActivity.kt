package com.example.mobileshipbattle

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobileshipbattle.databinding.ActivityHomeBinding
import com.example.mobileshipbattle.databinding.ActivityMainBinding

class HomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playOfflineBtn.setOnClickListener {
            CreateOfflineGame()
        }
    }

    fun  CreateOfflineGame() {
        GameData.SaveGameModel(
            GameModel(
                gameStatus = GameStatus.JOINED
            )
        )

        StartGame()
    }

    fun StartGame() {
        startActivity((Intent(this,GameActivity::class.java)))
    }
}