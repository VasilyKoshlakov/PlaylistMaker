package com.practicum.playlistmaker.presentation.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.media.MediaActivity
import com.practicum.playlistmaker.presentation.search.SearchActivity
import com.practicum.playlistmaker.presentation.settings.SettingsActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupButton(R.id.button_search, SearchActivity::class.java)
        setupButton(R.id.button_media, MediaActivity::class.java)
        setupButton(R.id.button_settings, SettingsActivity::class.java)
    }

    private fun setupButton(buttonId: Int, activityClass: Class<*>) {
        findViewById<Button>(buttonId).setOnClickListener {
            startActivity(Intent(this, activityClass))
        }
    }
}
