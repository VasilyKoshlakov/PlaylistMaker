package com.practicum.playlistmaker.media.di

import com.practicum.playlistmaker.media.ui.FavoritesViewModel
import com.practicum.playlistmaker.playlists.ui.CreatePlaylistViewModel
import com.practicum.playlistmaker.media.ui.PlaylistsViewModel
import com.practicum.playlistmaker.playlists.ui.EditPlaylistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mediaModule = module {
    viewModel { PlaylistsViewModel(get()) }
    viewModel { FavoritesViewModel(get(), get()) }
    viewModel { CreatePlaylistViewModel(get()) }
    viewModel { EditPlaylistViewModel(get()) }
}