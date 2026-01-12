package com.practicum.playlistmaker.playlists.di

import com.practicum.playlistmaker.playlists.data.db.PlaylistsRepositoryImpl
import com.practicum.playlistmaker.playlists.domain.PlaylistsInteractor
import com.practicum.playlistmaker.playlists.domain.PlaylistsInteractorImpl
import com.practicum.playlistmaker.playlists.domain.PlaylistsRepository
import org.koin.dsl.module

val playlistsModule = module {

    single<PlaylistsRepository> {
        PlaylistsRepositoryImpl(
            playlistsDao = get(),
            playlistTracksDao = get()
        )
    }

    single<PlaylistsInteractor> {
        PlaylistsInteractorImpl(repository = get())
    }
}