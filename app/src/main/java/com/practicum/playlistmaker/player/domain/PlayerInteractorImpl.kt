package com.practicum.playlistmaker.player.domain

class PlayerInteractorImpl(
    private val mediaPlayerController: MediaPlayerController
) : PlayerInteractor {

    override fun preparePlayer(previewUrl: String?, callback: (Boolean) -> Unit) {
        mediaPlayerController.preparePlayer(previewUrl, callback)
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