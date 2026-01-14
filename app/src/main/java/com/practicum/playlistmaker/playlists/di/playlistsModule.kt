package com.practicum.playlistmaker.playlists.di

import com.practicum.playlistmaker.playlists.data.db.PlaylistsRepositoryImpl
import com.practicum.playlistmaker.playlists.domain.PlaylistsInteractor
import com.practicum.playlistmaker.playlists.domain.PlaylistsInteractorImpl
import com.practicum.playlistmaker.playlists.domain.PlaylistsRepository
import com.practicum.playlistmaker.playlists.ui.PlaylistInfoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val playlistsModule = module {

    single<PlaylistsRepository> {
        PlaylistsRepositoryImpl(
            playlistsDao = get(),
            playlistTracksDao = get(),
            favoriteTracksDao = get()
        )
    }

    single<PlaylistsInteractor> {
        PlaylistsInteractorImpl(
            repository = get(),
            favoriteTracksDao = get()
        )
    }

    viewModel {
        PlaylistInfoViewModel(
            playlistsDao = get(),
            playlistTracksDao = get(),
            favoriteTracksDao = get(),
            playlistsInteractor = get()
        )
    }
}