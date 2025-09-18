package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.model.Track

interface SearchInteractor {
    fun searchTracks(query: String, callback: (List<Track>) -> Unit)
    fun getSearchHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearSearchHistory()
}