package com.practicum.playlistmaker.search.data

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private companion object {
        const val DARK_THEME_KEY = "dark_theme"
    }

    fun isDarkThemeEnabled(): Boolean {
        return sharedPreferences.getBoolean(DARK_THEME_KEY, false)
    }

    fun setDarkThemeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(DARK_THEME_KEY, enabled).apply()
    }
}
