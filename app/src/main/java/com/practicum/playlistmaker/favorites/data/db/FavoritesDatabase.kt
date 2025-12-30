package com.practicum.playlistmaker.favorites.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.practicum.playlistmaker.playlists.data.db.PlaylistEntity
import com.practicum.playlistmaker.playlists.data.db.PlaylistTrackEntity

@Database(
    entities = [
        FavoriteTrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract fun favoriteTracksDao(): FavoriteTracksDao
    abstract fun playlistsDao(): com.practicum.playlistmaker.playlists.data.db.PlaylistsDao
    abstract fun playlistTracksDao(): com.practicum.playlistmaker.playlists.data.db.PlaylistTracksDao
}