package com.practicum.playlistmaker.favorites.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.practicum.playlistmaker.playlists.data.db.PlaylistEntity
import com.practicum.playlistmaker.playlists.data.db.PlaylistTrackDetailsEntity
import com.practicum.playlistmaker.playlists.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.playlists.data.db.PlaylistTrackDetailsDao
import com.practicum.playlistmaker.playlists.data.db.PlaylistsDao
import com.practicum.playlistmaker.playlists.data.db.PlaylistTracksDao

@Database(
    entities = [
        FavoriteTrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        PlaylistTrackDetailsEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract fun favoriteTracksDao(): FavoriteTracksDao
    abstract fun playlistsDao(): PlaylistsDao
    abstract fun playlistTracksDao(): PlaylistTracksDao
    abstract fun playlistTrackDetailsDao(): PlaylistTrackDetailsDao
}