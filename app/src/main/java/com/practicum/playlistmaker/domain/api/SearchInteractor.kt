package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.model.Track

interface SearchInteractor {
    suspend fun searchTracks(query: String): List<Track>
    fun getSearchHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearSearchHistory()
}