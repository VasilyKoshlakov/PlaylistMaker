package com.practicum.playlistmaker.player.ui

data class PlayerState(
    val isPlaying: Boolean,
    val currentPosition: Int,
    val isPrepared: Boolean,
    val formattedCurrentTime: String
)
