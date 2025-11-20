package com.practicum.playlistmaker.search.ui

import com.practicum.playlistmaker.search.domain.Track

sealed interface SearchState {
    val searchQuery: String

    data class History(
        val tracks: List<Track>,
        override val searchQuery: String
    ) : SearchState

    data class Loading(
        override val searchQuery: String
    ) : SearchState

    data class Content(
        val tracks: List<Track>,
        override val searchQuery: String
    ) : SearchState

    data class Empty(
        override val searchQuery: String
    ) : SearchState

    data class Error(
        override val searchQuery: String
    ) : SearchState
}