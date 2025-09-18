package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.SearchInteractor
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.api.TrackRepository

class SearchInteractorImpl(private val repository: TrackRepository) : SearchInteractor {

    override fun searchTracks(query: String, callback: (List<Track>) -> Unit) {
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