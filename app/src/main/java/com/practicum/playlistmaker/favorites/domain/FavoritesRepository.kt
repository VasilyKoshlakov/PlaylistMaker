package com.practicum.playlistmaker.favorites.domain

import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    suspend fun addToFavorites(track: Track)
    suspend fun removeFromFavorites(trackId: Int)
    fun getAllFavorites(): Flow<List<Track>>
    suspend fun getAllFavoriteIds(): List<Int>
    fun isFavorite(trackId: Int): Flow<Boolean>
}