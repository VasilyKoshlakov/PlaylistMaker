package com.practicum.playlistmaker.search.di

import com.practicum.playlistmaker.search.data.ItunesApiService
import com.practicum.playlistmaker.search.data.RetrofitClient
import org.koin.dsl.module

val networkModule = module {
    single<ItunesApiService> { RetrofitClient.itunesApiService }
}