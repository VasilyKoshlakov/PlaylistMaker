package com.practicum.playlistmaker.player.di

import com.practicum.playlistmaker.player.data.MediaPlayerControllerImpl
import com.practicum.playlistmaker.player.domain.MediaPlayerController
import com.practicum.playlistmaker.player.domain.PlayerInteractor
import com.practicum.playlistmaker.player.domain.PlayerInteractorImpl
import com.practicum.playlistmaker.player.ui.PlayerViewModel
import org.koin.dsl.module

val playerModule = module {

    factory<MediaPlayerController> {
        MediaPlayerControllerImpl(mediaPlayerFactory = get())
    }

    factory<PlayerInteractor> {
        PlayerInteractorImpl(mediaPlayerController = get())
    }

    single {
        PlayerViewModel(
            playerInteractor = get(),
            favoritesInteractor = get(),
            gson = get()
        )
    }
}