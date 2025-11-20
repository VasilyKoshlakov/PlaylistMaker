package com.practicum.playlistmaker.player.domain

interface MediaPlayerController {
    suspend fun preparePlayer(previewUrl: String?): Boolean
    fun startPlayer()
    fun pausePlayer()
    fun releasePlayer()
    fun getCurrentPosition(): Int
    fun isPlaying(): Boolean
    fun setOnCompletionListener(listener: () -> Unit)
    fun getFormattedTime(millis: Long): String
}