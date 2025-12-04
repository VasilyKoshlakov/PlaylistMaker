package com.practicum.playlistmaker.favorites.data

import com.practicum.playlistmaker.favorites.data.db.FavoriteTrackEntity
import com.practicum.playlistmaker.favorites.data.db.FavoriteTracksDao
import com.practicum.playlistmaker.favorites.domain.FavoritesRepository
import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val dao: FavoriteTracksDao
) : FavoritesRepository {

    override suspend fun addToFavorites(track: Track) {
        dao.insert(FavoriteTrackEntity.fromTrack(track))
    }

    override suspend fun removeFromFavorites(trackId: Int) {
        dao.delete(trackId)
    }

    override fun getAllFavorites(): Flow<List<Track>> {
        return dao.getAll().map { entities ->
            entities.map { it.toTrack() }
        }
    }

    override suspend fun getAllFavoriteIds(): List<Int> {
        return dao.getAllIds()
    }

    override fun isFavorite(trackId: Int): Flow<Boolean> {
        return dao.isFavorite(trackId)
    }
}