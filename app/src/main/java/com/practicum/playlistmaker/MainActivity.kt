package com.practicum.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

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
