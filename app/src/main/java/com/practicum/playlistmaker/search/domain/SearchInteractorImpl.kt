package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.search.data.TrackRepositoryImpl
import kotlinx.coroutines.flow.Flow

class SearchInteractorImpl(private val repository: TrackRepository) : SearchInteractor {

    override fun searchTracks(query: String): Flow<TrackRepositoryImpl.SearchResult> {
        return repository.searchTracks(query)
    }

    override fun getSearchHistory(): List<Track> {
        return repository.getSearchHistory()
    }

    override fun addTrackToHistory(track: Track) {
        repository.addTrackToHistory(track)
    }

    override fun clearSearchHistory() {
        repository.clearSearchHistory()
    }
}