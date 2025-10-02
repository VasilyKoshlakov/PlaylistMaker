package com.practicum.playlistmaker.player.domain

import com.practicum.playlistmaker.player.data.TrackRepositoryImpl

interface TrackRepository {
    fun searchTracks(query: String, callback: (TrackRepositoryImpl.SearchResult) -> Unit)
    fun getSearchHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearSearchHistory()
}
