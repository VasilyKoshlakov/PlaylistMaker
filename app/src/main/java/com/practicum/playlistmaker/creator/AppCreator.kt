package com.practicum.playlistmaker.creator

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.player.data.MediaPlayerControllerImpl
import com.practicum.playlistmaker.search.data.SearchHistory
import com.practicum.playlistmaker.search.data.RetrofitClient
import com.practicum.playlistmaker.player.data.TrackRepositoryImpl
import com.practicum.playlistmaker.player.domain.PlayerInteractor
import com.practicum.playlistmaker.player.domain.PlayerInteractorImpl
import com.practicum.playlistmaker.search.domain.SearchInteractor
import com.practicum.playlistmaker.search.domain.SearchInteractorImpl
import com.practicum.playlistmaker.settings.domain.SettingsInteractor
import com.practicum.playlistmaker.player.domain.TrackRepository
import com.practicum.playlistmaker.settings.data.AppPreferences
import com.practicum.playlistmaker.settings.data.ResourcesProvider
import com.practicum.playlistmaker.settings.domain.SettingsInteractorImpl

object AppCreator {

    private lateinit var trackRepository: TrackRepository
    private lateinit var searchInteractor: SearchInteractor
    private lateinit var settingsInteractor: SettingsInteractor
    private lateinit var playerInteractor: PlayerInteractor

    fun init(context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val searchHistory = SearchHistory(sharedPreferences)
        val apiService = RetrofitClient.itunesApiService

        val appPreferences = AppPreferences(context)
        val resourcesProvider = ResourcesProvider(context)
        val mediaPlayerController = MediaPlayerControllerImpl()

        trackRepository = TrackRepositoryImpl(apiService, searchHistory)

        searchInteractor = SearchInteractorImpl(trackRepository)

        settingsInteractor = SettingsInteractorImpl(
            darkThemeProvider = { appPreferences.isDarkThemeEnabled() },
            darkThemeSetter = { enabled ->
                appPreferences.setDarkThemeEnabled(enabled)
            },
            shareMessageProvider = { resourcesProvider.getShareMessage() },
            supportEmailProvider = { resourcesProvider.getSupportEmail() },
            emailSubjectProvider = { resourcesProvider.getEmailSubject() },
            emailBodyProvider = { resourcesProvider.getEmailBody() },
            userAgreementUrlProvider = { resourcesProvider.getUserAgreementUrl() },
            emailChooserTitleProvider = { resourcesProvider.getEmailChooserTitle() },
            emailClientNotFoundMessageProvider = { resourcesProvider.getEmailClientNotFoundMessage() },
            browserNotFoundMessageProvider = { resourcesProvider.getBrowserNotFoundMessage() },
            themeApplier = { enabled ->
                AppCompatDelegate.setDefaultNightMode(
                    if (enabled) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        )

        playerInteractor = PlayerInteractorImpl(mediaPlayerController)
    }

    fun provideSearchInteractor(): SearchInteractor = searchInteractor
    fun provideSettingsInteractor(): SettingsInteractor = settingsInteractor
    fun providePlayerInteractor(): PlayerInteractor = playerInteractor
}
