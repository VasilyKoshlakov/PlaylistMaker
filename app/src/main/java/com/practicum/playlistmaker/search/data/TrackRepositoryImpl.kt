package com.practicum.playlistmaker.search.data

import com.practicum.playlistmaker.favorites.domain.FavoritesRepository
import com.practicum.playlistmaker.search.domain.Track
import com.practicum.playlistmaker.search.domain.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TrackRepositoryImpl(
    private val apiService: ItunesApiService,
    private val searchHistory: SearchHistory,
    private val favoritesRepository: FavoritesRepository
) : TrackRepository {

    override suspend fun searchTracks(query: String): Flow<TrackRepository.SearchResult> = flow {
        try {
            emit(TrackRepository.SearchResult.Loading)

            val response = apiService.search(query)

            val favoriteIds = favoritesRepository.getAllFavoriteIds()

            val tracks = response.results.map { trackDto ->
                Track(
                    trackId = trackDto.trackId,
                    trackName = trackDto.trackName,
                    artistName = trackDto.artistName,
                    trackTimeMillis = trackDto.trackTimeMillis,
                    artworkUrl100 = trackDto.artworkUrl100,
                    collectionName = trackDto.collectionName,
                    releaseDate = trackDto.releaseDate,
                    primaryGenreName = trackDto.primaryGenreName,
                    country = trackDto.country,
                    previewUrl = trackDto.previewUrl,
                    isFavorite = favoriteIds.contains(trackDto.trackId)
                )
            }

            if (tracks.isEmpty()) {
                emit(TrackRepository.SearchResult.Empty)
            } else {
                emit(TrackRepository.SearchResult.Success(tracks))
            }
        } catch (e: Exception) {
            emit(TrackRepository.SearchResult.Error("Network error: ${e.message}"))
        }
    }

    override suspend fun getSearchHistory(): List<Track> {
        val history = searchHistory.getHistory()
        val favoriteIds = favoritesRepository.getAllFavoriteIds()

        return history.map { track ->
            track.copy(isFavorite = favoriteIds.contains(track.trackId))
        }
    }

    override fun addTrackToHistory(track: Track) {
        searchHistory.addTrack(track.copy(isFavorite = false))
    }

    override fun clearSearchHistory() {
        searchHistory.clearHistory()
    }
}