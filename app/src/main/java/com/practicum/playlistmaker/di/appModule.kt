package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.favorites.di.favoritesModule
import com.practicum.playlistmaker.media.di.mediaModule
import com.practicum.playlistmaker.player.di.playerModule
import com.practicum.playlistmaker.search.di.networkModule
import com.practicum.playlistmaker.search.di.searchModule
import com.practicum.playlistmaker.settings.di.settingsModule
import org.koin.dsl.module

val appModule = module {
    includes(
        baseModule,
        networkModule,
        searchModule,
        playerModule,
        settingsModule,
        mediaModule,
        favoritesModule
    )
}