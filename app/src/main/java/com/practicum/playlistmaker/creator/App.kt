package com.practicum.playlistmaker.creator

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        AppCreator.init(this)

        val settingsInteractor = AppCreator.provideSettingsInteractor()
        val isDarkTheme = settingsInteractor.isDarkTheme()

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
