package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.SearchInteractor
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.api.TrackRepository

class SearchInteractorImpl(private val repository: TrackRepository) : SearchInteractor {

    override suspend fun searchTracks(query: String): List<Track> {
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