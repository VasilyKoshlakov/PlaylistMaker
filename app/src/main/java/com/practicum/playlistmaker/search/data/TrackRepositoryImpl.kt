package com.practicum.playlistmaker.search.data

import com.practicum.playlistmaker.search.domain.Track
import android.os.Handler
import android.os.Looper
import com.practicum.playlistmaker.search.domain.TrackRepository

class TrackRepositoryImpl(
    private val apiService: ItunesApiService,
    private val searchHistory: SearchHistory
) : TrackRepository {

    private val handler = Handler(Looper.getMainLooper())

    override fun searchTracks(query: String, callback: (SearchResult) -> Unit) {
        Thread {
            try {
                val response = apiService.search(query).execute()
                if (response.isSuccessful) {
                    val tracks = response.body()?.results?.map { trackDto ->
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
                    } ?: emptyList()

                    handler.post { callback(SearchResult.Success(tracks)) }
                } else {
                    handler.post { callback(SearchResult.Error("HTTP error: ${response.code()}")) }
                }
            } catch (e: Exception) {
                handler.post { callback(SearchResult.Error("Network error: ${e.message}")) }
            }
        }.start()
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
        data class Success(val tracks: List<Track>) : SearchResult()
        data class Error(val message: String) : SearchResult()
    }
}
