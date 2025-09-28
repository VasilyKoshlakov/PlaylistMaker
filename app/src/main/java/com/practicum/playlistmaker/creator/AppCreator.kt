package com.practicum.playlistmaker.creator

import android.content.Context
import com.practicum.playlistmaker.player.data.MediaPlayerController
import com.practicum.playlistmaker.search.data.SearchHistory
import com.practicum.playlistmaker.search.data.RetrofitClient
import com.practicum.playlistmaker.player.data.TrackRepositoryImpl
import com.practicum.playlistmaker.player.domain.PlayerInteractor
import com.practicum.playlistmaker.player.domain.PlayerInteractorImpl
import com.practicum.playlistmaker.search.domain.SearchInteractor
import com.practicum.playlistmaker.search.domain.SearchInteractorImpl
import com.practicum.playlistmaker.settings.domain.SettingsInteractor
import com.practicum.playlistmaker.player.domain.TrackRepository
import com.practicum.playlistmaker.search.data.AppPreferences
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
        val mediaPlayerController = MediaPlayerController()

        trackRepository = TrackRepositoryImpl(apiService, searchHistory)

        searchInteractor = SearchInteractorImpl(trackRepository)

        settingsInteractor = SettingsInteractorImpl(
            darkThemeProvider = { appPreferences.isDarkThemeEnabled() },
            darkThemeSetter = { enabled ->
                appPreferences.setDarkThemeEnabled(enabled)
                App.setDarkTheme(enabled)
            },
            shareMessageProvider = { resourcesProvider.getShareMessage() },
            supportEmailProvider = { resourcesProvider.getSupportEmail() },
            emailSubjectProvider = { resourcesProvider.getEmailSubject() },
            emailBodyProvider = { resourcesProvider.getEmailBody() },
            userAgreementUrlProvider = { resourcesProvider.getUserAgreementUrl() },
            emailChooserTitleProvider = { resourcesProvider.getEmailChooserTitle() },
            emailClientNotFoundMessageProvider = { resourcesProvider.getEmailClientNotFoundMessage() },
            browserNotFoundMessageProvider = { resourcesProvider.getBrowserNotFoundMessage() }
        )

        playerInteractor = PlayerInteractorImpl(mediaPlayerController)
    }

    fun provideSearchInteractor(): SearchInteractor = searchInteractor
    fun provideSettingsInteractor(): SettingsInteractor = settingsInteractor
    fun providePlayerInteractor(): PlayerInteractor = playerInteractor
}
