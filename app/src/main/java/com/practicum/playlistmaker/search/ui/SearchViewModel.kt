package com.practicum.playlistmaker.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.practicum.playlistmaker.search.domain.SearchInteractor
import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val gson: Gson
) : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    private var currentSearchQuery = ""
    private var searchJob: Job? = null
    private var clickJob: Job? = null

    private val searchDelayMillis = 2000L
    private val clickDebounceMillis = 500L

    private var shouldRestoreState = true

    init {
        if (shouldRestoreState) {
            showSearchHistory()
            shouldRestoreState = true
        }
    }

    fun searchTracks(query: String) {
        currentSearchQuery = query.trim()

        if (currentSearchQuery.isEmpty()) {
            showSearchHistory()
            return
        }

        searchJob?.cancel()

        _searchState.value = SearchState.Loading(searchQuery = currentSearchQuery)

        searchJob = viewModelScope.launch {
            delay(searchDelayMillis)
            performSearch(currentSearchQuery)
        }
    }

    private suspend fun performSearch(query: String) {
        searchInteractor.searchTracks(query).collect { result ->
            val newState = when (result) {
                is com.practicum.playlistmaker.search.data.TrackRepositoryImpl.SearchResult.Loading ->
                    SearchState.Loading(searchQuery = query)
                is com.practicum.playlistmaker.search.data.TrackRepositoryImpl.SearchResult.Success -> {
                    if (result.tracks.isEmpty()) {
                        SearchState.Empty(searchQuery = query)
                    } else {
                        SearchState.Content(tracks = result.tracks, searchQuery = query)
                    }
                }
                is com.practicum.playlistmaker.search.data.TrackRepositoryImpl.SearchResult.Error -> {
                    SearchState.Error(searchQuery = query)
                }
                is com.practicum.playlistmaker.search.data.TrackRepositoryImpl.SearchResult.Empty -> {
                    SearchState.Empty(searchQuery = query)
                }
            }
            _searchState.postValue(newState)
        }
    }

    fun onTrackClick(track: Track) {
        clickJob?.cancel()
        clickJob = viewModelScope.launch {
            delay(clickDebounceMillis)
            addTrackToHistory(track)
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
        if (query.isEmpty()) {
            showSearchHistory()
        }
    }
    fun restoreSearchState() {
        when (val currentState = _searchState.value) {
            is SearchState.Content -> {
                _searchState.value = currentState
            }
            is SearchState.Empty -> {
                _searchState.value = currentState
            }
            is SearchState.Error -> {
                _searchState.value = currentState
            }
            is SearchState.Loading -> {
                showSearchHistory()
            }
            is SearchState.History -> {
                _searchState.value = currentState
            }
            null -> {
                showSearchHistory()
            }
        }
    }

    fun refreshSearch() {
        when (val currentState = _searchState.value) {
            is SearchState.Content -> {
                searchTracks(currentState.searchQuery)
            }
            is SearchState.Empty -> {
                searchTracks(currentState.searchQuery)
            }
            is SearchState.Error -> {
                searchTracks(currentState.searchQuery)
            }
            else -> {
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        clickJob?.cancel()
    }

    fun trackToJson(track: Track): String {
        return gson.toJson(track)
    }
}