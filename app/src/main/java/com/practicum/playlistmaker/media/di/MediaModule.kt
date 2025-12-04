package com.practicum.playlistmaker.media.di

import com.practicum.playlistmaker.media.ui.FavoritesViewModel
import com.practicum.playlistmaker.media.ui.PlaylistsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mediaModule = module {
    viewModel { PlaylistsViewModel() }
    viewModel { FavoritesViewModel(favoritesInteractor = get()) }
}