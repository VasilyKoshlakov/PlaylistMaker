package com.practicum.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val imageSearch = findViewById<Button>(R.id.button_search)

        imageSearch.setOnClickListener {
            Toast.makeText(this@MainActivity, "Нажали на Поиск!", Toast.LENGTH_SHORT).show()
        }
        val imageMedia = findViewById<Button>(R.id.button_media)

        val imageClickListener: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(v: View?) {
                Toast.makeText(this@MainActivity, "Нажали на Медиатеку!", Toast.LENGTH_SHORT).show()
            }
        }
        imageMedia.setOnClickListener(imageClickListener)

        val imageSettings= findViewById<Button>(R.id.button_settings)

        imageSettings.setOnClickListener {
            Toast.makeText(this@MainActivity, "Нажали на Настройки!", Toast.LENGTH_SHORT).show()
        }
    }
}
