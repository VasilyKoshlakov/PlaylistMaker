package com.practicum.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        val sharedPref = getSharedPreferences(SettingsActivity.THEME_PREFS_KEY, MODE_PRIVATE)
        val isDarkTheme = sharedPref.getBoolean(SettingsActivity.DARK_THEME_KEY, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}