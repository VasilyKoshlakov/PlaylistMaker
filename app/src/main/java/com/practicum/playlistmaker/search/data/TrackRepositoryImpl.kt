package com.practicum.playlistmaker.search.data

import com.practicum.playlistmaker.search.domain.Track
import com.practicum.playlistmaker.search.domain.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TrackRepositoryImpl(
    private val apiService: ItunesApiService,
    private val searchHistory: SearchHistory
) : TrackRepository {

    override fun searchTracks(query: String): Flow<SearchResult> = flow {
        try {
            emit(SearchResult.Loading)

            val response = apiService.search(query)
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
                    previewUrl = trackDto.previewUrl
                )
            }

            if (tracks.isEmpty()) {
                emit(SearchResult.Empty)
            } else {
                emit(SearchResult.Success(tracks))
            }
        } catch (e: Exception) {
            emit(SearchResult.Error("Network error: ${e.message}"))
        }
    }

    override fun getSearchHistory(): List<Track> {
        return searchHistory.getHistory()
    }

    override fun addTrackToHistory(track: Track) {
        searchHistory.addTrack(track)
    }

    override fun clearSearchHistory() {
        searchHistory.clearHistory()
    }

    sealed class SearchResult {
        object Loading : SearchResult()
        object Empty : SearchResult()
        data class Success(val tracks: List<Track>) : SearchResult()
        data class Error(val message: String) : SearchResult()
    }
}