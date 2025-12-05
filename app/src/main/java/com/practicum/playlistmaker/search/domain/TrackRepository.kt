package com.practicum.playlistmaker.search.domain

import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    suspend fun searchTracks(query: String): Flow<SearchResult>
    suspend fun getSearchHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearSearchHistory()

    sealed class SearchResult {
        object Loading : SearchResult()
        object Empty : SearchResult()
        data class Success(val tracks: List<Track>) : SearchResult()
        data class Error(val message: String) : SearchResult()
    }
}