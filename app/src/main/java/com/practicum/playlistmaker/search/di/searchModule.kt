package com.practicum.playlistmaker.search.di

import com.practicum.playlistmaker.search.data.SearchHistory
import com.practicum.playlistmaker.search.data.TrackRepositoryImpl
import com.practicum.playlistmaker.search.domain.SearchInteractor
import com.practicum.playlistmaker.search.domain.SearchInteractorImpl
import com.practicum.playlistmaker.search.domain.TrackRepository
import com.practicum.playlistmaker.search.ui.SearchViewModel
import org.koin.dsl.module

val searchModule = module {

    single {
        SearchHistory(
            sharedPreferences = get(),
            gson = get()
        )
    }

    single<TrackRepository> {
        TrackRepositoryImpl(
            apiService = get(),
            searchHistory = get(),
            favoritesRepository = get()
        )
    }

    factory<SearchInteractor> {
        SearchInteractorImpl(repository = get())
    }

    single {
        SearchViewModel(
            searchInteractor = get(),
            gson = get()
        )
    }
}