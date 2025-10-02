package com.practicum.playlistmaker.search.ui

import com.practicum.playlistmaker.player.domain.Track
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.player.data.TrackRepositoryImpl
import com.practicum.playlistmaker.search.domain.SearchInteractor
import java.util.Timer
import java.util.TimerTask

class SearchViewModel(private val searchInteractor: SearchInteractor) : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    private var currentSearchQuery = ""
    private var searchTimer: Timer? = null
    private val searchDelayMillis = 2000L

    init {
        _searchState.value = SearchState.History(
            tracks = searchInteractor.getSearchHistory(),
            searchQuery = ""
        )
    }

    fun searchTracks(query: String) {
        currentSearchQuery = query.trim()

        if (currentSearchQuery.isEmpty()) {
            showSearchHistory()
            return
        }

        searchTimer?.cancel()

        _searchState.value = SearchState.Loading(searchQuery = currentSearchQuery)

        searchTimer = Timer()
        searchTimer?.schedule(object : TimerTask() {
            override fun run() {
                performSearch(currentSearchQuery)
            }
        }, searchDelayMillis)
    }

    private fun performSearch(query: String) {
        searchInteractor.searchTracks(query) { result ->
            val newState = when (result) {
                is TrackRepositoryImpl.SearchResult.Success -> {
                    if (result.tracks.isEmpty()) {
                        SearchState.Empty(searchQuery = query)
                    } else {
                        SearchState.Content(tracks = result.tracks, searchQuery = query)
                    }
                }
                is TrackRepositoryImpl.SearchResult.Error -> {
                    SearchState.Error(searchQuery = query)
                }
            }
            _searchState.postValue(newState)
        }
    }

    fun showSearchHistory() {
        val history = searchInteractor.getSearchHistory()
        _searchState.value = SearchState.History(
            tracks = history,
            searchQuery = currentSearchQuery
        )
    }

    fun addTrackToHistory(track: Track) {
        searchInteractor.addTrackToHistory(track)
    }

    fun clearSearchHistory() {
        searchInteractor.clearSearchHistory()
        showSearchHistory()
    }

    fun updateSearchQuery(query: String) {
        currentSearchQuery = query
    }

    override fun onCleared() {
        super.onCleared()
        searchTimer?.cancel()
    }
}

