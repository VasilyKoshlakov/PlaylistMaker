package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.player.domain.Track

interface SearchInteractor {
    fun searchTracks(query: String, callback: (List<Track>) -> Unit)
    fun getSearchHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearSearchHistory()
}