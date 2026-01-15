package com.practicum.playlistmaker.playlists.di

import com.practicum.playlistmaker.playlists.ui.EditPlaylistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val playlistEditModule = module {
    viewModel { EditPlaylistViewModel(playlistsInteractor = get()) }
}