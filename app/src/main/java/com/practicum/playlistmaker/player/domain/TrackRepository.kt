package com.practicum.playlistmaker.player.domain

interface TrackRepository {
    fun searchTracks(query: String, callback: (List<Track>) -> Unit)
    fun getSearchHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearSearchHistory()
}
