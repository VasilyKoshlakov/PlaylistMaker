package com.practicum.playlistmaker.favorites.domain

import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FavoritesInteractorImpl(
    private val repository: FavoritesRepository
) : FavoritesInteractor {

    override suspend fun toggleFavorite(track: Track) {
        val isFavorite = repository.isFavorite(track.trackId)
        val isCurrentlyFavorite = isFavorite.firstOrNull() ?: false

        if (isCurrentlyFavorite) {
            removeFromFavorites(track.trackId)
        } else {
            addToFavorites(track)
        }
    }

    override suspend fun addToFavorites(track: Track) {
        repository.addToFavorites(track)
    }

    override suspend fun removeFromFavorites(trackId: Int) {
        repository.removeFromFavorites(trackId)
    }

    override fun getAllFavorites(): Flow<List<Track>> {
        return repository.getAllFavorites()
    }

    override suspend fun getAllFavoriteIds(): List<Int> {
        return repository.getAllFavoriteIds()
    }

    override fun isFavorite(trackId: Int): Flow<Boolean> {
        return repository.isFavorite(trackId)
    }
}