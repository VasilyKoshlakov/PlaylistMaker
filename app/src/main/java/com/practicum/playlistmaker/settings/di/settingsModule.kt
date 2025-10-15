package com.practicum.playlistmaker.settings.di

import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.settings.data.AppPreferences
import com.practicum.playlistmaker.settings.data.ResourcesProvider
import com.practicum.playlistmaker.settings.domain.SettingsInteractor
import com.practicum.playlistmaker.settings.domain.SettingsInteractorImpl
import com.practicum.playlistmaker.settings.ui.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val settingsModule = module {

    // Data layer
    single { AppPreferences(androidContext()) }
    single { ResourcesProvider(androidContext()) }

    // Domain layer
    factory<SettingsInteractor> {
        SettingsInteractorImpl(
            darkThemeProvider = { get<AppPreferences>().isDarkThemeEnabled() },
            darkThemeSetter = { enabled -> get<AppPreferences>().setDarkThemeEnabled(enabled) },
            shareMessageProvider = { get<ResourcesProvider>().getShareMessage() },
            supportEmailProvider = { get<ResourcesProvider>().getSupportEmail() },
            emailSubjectProvider = { get<ResourcesProvider>().getEmailSubject() },
            emailBodyProvider = { get<ResourcesProvider>().getEmailBody() },
            userAgreementUrlProvider = { get<ResourcesProvider>().getUserAgreementUrl() },
            emailChooserTitleProvider = { get<ResourcesProvider>().getEmailChooserTitle() },
            emailClientNotFoundMessageProvider = { get<ResourcesProvider>().getEmailClientNotFoundMessage() },
            browserNotFoundMessageProvider = { get<ResourcesProvider>().getBrowserNotFoundMessage() },
            themeApplier = { enabled ->
                AppCompatDelegate.setDefaultNightMode(
                    if (enabled) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        )
    }

    // Presentation layer
    single { SettingsViewModel(settingsInteractor = get()) }
}