package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.search.data.TrackRepositoryImpl
import kotlinx.coroutines.flow.Flow

interface SearchInteractor {
    fun searchTracks(query: String): Flow<TrackRepositoryImpl.SearchResult>
    fun getSearchHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearSearchHistory()
}