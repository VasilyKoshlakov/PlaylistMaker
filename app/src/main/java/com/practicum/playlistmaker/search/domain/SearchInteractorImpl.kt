package com.practicum.playlistmaker.search.domain

import kotlinx.coroutines.flow.Flow

class SearchInteractorImpl(private val repository: TrackRepository) : SearchInteractor {

    override suspend fun searchTracks(query: String): Flow<TrackRepository.SearchResult> {
        return repository.searchTracks(query)
    }

    override suspend fun getSearchHistory(): List<Track> {
        return repository.getSearchHistory()
    }

    override fun addTrackToHistory(track: Track) {
        repository.addTrackToHistory(track)
    }

    override fun clearSearchHistory() {
        repository.clearSearchHistory()
    }
}