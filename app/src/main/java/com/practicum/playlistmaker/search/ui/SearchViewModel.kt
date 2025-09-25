package com.practicum.playlistmaker.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.search.domain.SearchInteractor
import com.practicum.playlistmaker.player.domain.Track
import java.util.Timer
import java.util.TimerTask

class SearchViewModel(private val searchInteractor: SearchInteractor) : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    private var currentSearchQuery = ""
    private var searchTimer: Timer? = null
    private val searchDelayMillis = 2000L

    init {
        _searchState.value = SearchState(
            tracks = emptyList(),
            isLoading = false,
            isError = false,
            showHistory = true,
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

        _searchState.value = SearchState(
            tracks = emptyList(),
            isLoading = true,
            isError = false,
            showHistory = false,
            searchQuery = currentSearchQuery
        )

        searchTimer = Timer()
        searchTimer?.schedule(object : TimerTask() {
            override fun run() {
                performSearch(currentSearchQuery)
            }
        }, searchDelayMillis)
    }

    private fun performSearch(query: String) {
        searchInteractor.searchTracks(query) { tracks ->
            val newState = if (tracks.isEmpty()) {
                SearchState(
                    tracks = emptyList(),
                    isLoading = false,
                    isError = true,
                    showHistory = false,
                    searchQuery = query
                )
            } else {
                SearchState(
                    tracks = tracks,
                    isLoading = false,
                    isError = false,
                    showHistory = false,
                    searchQuery = query
                )
            }
            _searchState.postValue(newState)
        }
    }

    fun showSearchHistory() {
        val history = searchInteractor.getSearchHistory()
        _searchState.value = SearchState(
            tracks = history,
            isLoading = false,
            isError = false,
            showHistory = true,
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
