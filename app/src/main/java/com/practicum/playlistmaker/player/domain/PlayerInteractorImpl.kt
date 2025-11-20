package com.practicum.playlistmaker.player.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlayerInteractorImpl(
    private val mediaPlayerController: MediaPlayerController
) : PlayerInteractor {

    override suspend fun preparePlayer(previewUrl: String?): Boolean = withContext(Dispatchers.IO) {
        mediaPlayerController.preparePlayer(previewUrl)
    }

    override fun startPlayer() {
        mediaPlayerController.startPlayer()
    }

    override fun pausePlayer() {
        mediaPlayerController.pausePlayer()
    }

    override fun releasePlayer() {
        mediaPlayerController.releasePlayer()
    }

    override fun getCurrentPosition(): Int = mediaPlayerController.getCurrentPosition()

    override fun isPlaying(): Boolean = mediaPlayerController.isPlaying()

    override fun setOnCompletionListener(listener: () -> Unit) {
        mediaPlayerController.setOnCompletionListener(listener)
    }

    override fun getFormattedTime(millis: Long): String = mediaPlayerController.getFormattedTime(millis)
}