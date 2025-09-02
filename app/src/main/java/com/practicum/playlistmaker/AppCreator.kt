package com.practicum.playlistmaker

import android.content.Context
import com.practicum.playlistmaker.data.local.SearchHistory
import com.practicum.playlistmaker.data.network.RetrofitClient
import com.practicum.playlistmaker.data.network.TrackRepositoryImpl
import com.practicum.playlistmaker.domain.api.SearchInteractor
import com.practicum.playlistmaker.domain.impl.SearchInteractorImpl
import com.practicum.playlistmaker.domain.api.TrackRepository

object AppCreator {

    private lateinit var trackRepository: TrackRepository
    private lateinit var searchInteractor: SearchInteractor

    fun init(context: Context) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val searchHistory = SearchHistory(sharedPreferences)
        val apiService = RetrofitClient.itunesApiService

        trackRepository = TrackRepositoryImpl(apiService, searchHistory)
        searchInteractor = SearchInteractorImpl(trackRepository)
    }

    fun provideSearchInteractor(): SearchInteractor {
        return searchInteractor
    }
}