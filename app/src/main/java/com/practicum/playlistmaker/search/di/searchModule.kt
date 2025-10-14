package com.practicum.playlistmaker.search.di

import com.practicum.playlistmaker.search.data.SearchHistory
import com.practicum.playlistmaker.search.data.TrackRepositoryImpl
import com.practicum.playlistmaker.search.domain.SearchInteractor
import com.practicum.playlistmaker.search.domain.SearchInteractorImpl
import com.practicum.playlistmaker.search.domain.TrackRepository
import com.practicum.playlistmaker.search.ui.SearchViewModel
import org.koin.dsl.module

val searchModule = module {

    // Data layer
    single {
        SearchHistory(
            sharedPreferences = get(),
            gson = get()
        )
    }

    single<TrackRepository> {
        TrackRepositoryImpl(
            apiService = get(),
            searchHistory = get()
        )
    }

    // Domain layer
    factory<SearchInteractor> {
        SearchInteractorImpl(repository = get())
    }

    // Presentation layer
    single {
        SearchViewModel(
            searchInteractor = get(),
            gson = get()
        )
    }
}