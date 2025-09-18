package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.model.Track

interface TrackRepository {
    fun searchTracks(query: String, callback: (List<Track>) -> Unit)
    fun getSearchHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearSearchHistory()
}