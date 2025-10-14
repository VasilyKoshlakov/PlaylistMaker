package com.practicum.playlistmaker.di

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.settings.domain.SettingsInteractor
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        setupTheme()
    }

    private fun setupTheme() {
        val koin = getKoin()
        val settingsInteractor = koin.get<SettingsInteractor>()
        val isDarkTheme = settingsInteractor.isDarkTheme()

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}