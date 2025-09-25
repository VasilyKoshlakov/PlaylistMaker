package com.practicum.playlistmaker.search.ui

import com.practicum.playlistmaker.player.domain.Track

data class SearchState(
    val tracks: List<Track>,
    val isLoading: Boolean,
    val isError: Boolean,
    val showHistory: Boolean,
    val searchQuery: String
)
