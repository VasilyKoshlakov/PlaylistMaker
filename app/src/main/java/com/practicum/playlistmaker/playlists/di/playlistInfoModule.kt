package com.practicum.playlistmaker.playlists.di

import com.practicum.playlistmaker.playlists.ui.PlaylistInfoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val playlistInfoModule = module {

    viewModel {
        PlaylistInfoViewModel(
            playlistsDao = get(),
            playlistTracksDao = get(),
            favoriteTracksDao = get(),
            playlistsInteractor = get()
        )
    }
}