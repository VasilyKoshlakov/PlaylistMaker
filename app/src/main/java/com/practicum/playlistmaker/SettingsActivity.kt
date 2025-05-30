package com.practicum.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val settingsBackButton = findViewById<ImageButton>(R.id.button_back_settings)

        settingsBackButton.setOnClickListener {
            finish()
        }
    }
}