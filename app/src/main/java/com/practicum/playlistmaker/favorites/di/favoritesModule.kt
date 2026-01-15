package com.practicum.playlistmaker.favorites.di

import androidx.room.Room
import com.practicum.playlistmaker.favorites.data.FavoritesRepositoryImpl
import com.practicum.playlistmaker.favorites.data.db.FavoritesDatabase
import com.practicum.playlistmaker.favorites.domain.FavoritesInteractor
import com.practicum.playlistmaker.favorites.domain.FavoritesInteractorImpl
import com.practicum.playlistmaker.favorites.domain.FavoritesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val favoritesModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            FavoritesDatabase::class.java,
            "favorites.db"
        )
            .fallbackToDestructiveMigration()
            .setQueryExecutor(Dispatchers.IO.asExecutor())
            .build()
    }

    single {
        get<FavoritesDatabase>().favoriteTracksDao()
    }

    single {
        get<FavoritesDatabase>().playlistsDao()
    }

    single {
        get<FavoritesDatabase>().playlistTracksDao()
    }

    single {
        get<FavoritesDatabase>().playlistTrackDetailsDao()
    }

    single<FavoritesRepository> {
        FavoritesRepositoryImpl(dao = get())
    }

    factory<FavoritesInteractor> {
        FavoritesInteractorImpl(repository = get())
    }
}