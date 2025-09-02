package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.local.SearchHistory
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.api.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrackRepositoryImpl(
    private val apiService: ItunesApiService,
    private val searchHistory: SearchHistory
) : TrackRepository {

    override suspend fun searchTracks(query: String): List<Track> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.search(query).execute()
            if (response.isSuccessful) {
                response.body()?.results?.map { trackDto ->
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
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
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
}