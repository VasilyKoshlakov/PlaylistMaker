package com.practicum.playlistmaker.player.ui

import com.practicum.playlistmaker.player.domain.Track

sealed interface PlayerState {
    val isPrepared: Boolean
    val formattedCurrentTime: String

    data class Loading(
        override val isPrepared: Boolean = false,
        override val formattedCurrentTime: String = Track.formatTime(0)
    ) : PlayerState

    data class Error(
        override val isPrepared: Boolean = false,
        override val formattedCurrentTime: String = Track.formatTime(0)
    ) : PlayerState

    data class Prepared(
        override val isPrepared: Boolean = true,
        override val formattedCurrentTime: String = Track.formatTime(0)
    ) : PlayerState

    data class Playing(
        val currentPosition: Int,
        override val isPrepared: Boolean = true,
        override val formattedCurrentTime: String
    ) : PlayerState

    data class Paused(
        val currentPosition: Int,
        override val isPrepared: Boolean = true,
        override val formattedCurrentTime: String
    ) : PlayerState

    companion object {
        fun createDefaultPreparedState(): PlayerState {
            return Prepared(formattedCurrentTime = Track.formatTime(0))
        }

        fun createDefaultLoadingState(): PlayerState {
            return Loading(formattedCurrentTime = Track.formatTime(0))
        }
    }
}
