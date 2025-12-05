package com.practicum.playlistmaker.favorites.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteTrackEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract fun favoriteTracksDao(): FavoriteTracksDao
}
