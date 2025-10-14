package com.practicum.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.player.data.MediaPlayerFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val baseModule = module {
    single<SharedPreferences> {
        androidContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    single<Gson> { Gson() }

    single { MediaPlayerFactory() }
}