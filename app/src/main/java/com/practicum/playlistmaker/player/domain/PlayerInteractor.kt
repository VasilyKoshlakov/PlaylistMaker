package com.practicum.playlistmaker.player.domain

interface PlayerInteractor {
    fun preparePlayer(previewUrl: String?, callback: (Boolean) -> Unit)
    fun startPlayer()
    fun pausePlayer()
    fun releasePlayer()
    fun getCurrentPosition(): Int
    fun isPlaying(): Boolean
    fun setOnCompletionListener(listener: () -> Unit)
    fun getFormattedTime(millis: Long): String
}