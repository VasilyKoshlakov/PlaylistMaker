package com.practicum.playlistmaker.domain.api

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