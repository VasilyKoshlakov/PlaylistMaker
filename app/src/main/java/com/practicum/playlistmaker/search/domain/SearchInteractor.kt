package com.practicum.playlistmaker.search.domain

import kotlinx.coroutines.flow.Flow

interface SearchInteractor {
    suspend fun searchTracks(query: String): Flow<TrackRepository.SearchResult>
    suspend fun getSearchHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearSearchHistory()
}