package com.practicum.playlistmaker.search.domain

import com.practicum.playlistmaker.search.data.TrackRepositoryImpl

class SearchInteractorImpl(private val repository: TrackRepository) : SearchInteractor {

    override fun searchTracks(query: String, callback: (TrackRepositoryImpl.SearchResult) -> Unit) {
        repository.searchTracks(query, callback)
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