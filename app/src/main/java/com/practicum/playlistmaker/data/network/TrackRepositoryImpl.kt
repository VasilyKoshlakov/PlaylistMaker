package com.practicum.playlistmaker.data.network

import android.os.Handler
import android.os.Looper
import com.practicum.playlistmaker.data.local.SearchHistory
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.api.TrackRepository
import com.practicum.playlistmaker.data.network.ItunesApiService

class TrackRepositoryImpl(
    private val apiService: ItunesApiService,
    private val searchHistory: SearchHistory
) : TrackRepository {

    private val handler = Handler(Looper.getMainLooper())

    override fun searchTracks(query: String, callback: (List<Track>) -> Unit) {
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

                    handler.post { callback(tracks) }
                } else {
                    handler.post { callback(emptyList()) }
                }
            } catch (e: Exception) {
                handler.post { callback(emptyList()) }
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
}