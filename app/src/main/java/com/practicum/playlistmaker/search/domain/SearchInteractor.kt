package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.search.data.TrackRepositoryImpl

interface SearchInteractor {
    fun searchTracks(query: String, callback: (TrackRepositoryImpl.SearchResult) -> Unit)
    fun getSearchHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearSearchHistory()
}