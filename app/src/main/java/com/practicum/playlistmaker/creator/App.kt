package com.practicum.playlistmaker.creator

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {
    companion object {
        private const val PREFS_NAME = "playlist_maker_prefs"
        private const val DARK_THEME_KEY = "dark_theme"

        private lateinit var sharedPreferences: SharedPreferences

        fun isDarkTheme(): Boolean {
            return sharedPreferences.getBoolean(DARK_THEME_KEY, false)
        }

        fun setDarkTheme(enabled: Boolean) {
            sharedPreferences.edit().putBoolean(DARK_THEME_KEY, enabled).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (enabled) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        setDarkTheme(isDarkTheme())

        AppCreator.init(this)
    }
}