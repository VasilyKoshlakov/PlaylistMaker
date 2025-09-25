package com.practicum.playlistmaker.player.domain

import android.media.MediaPlayer
import java.util.Locale

class PlayerInteractorImpl : PlayerInteractor {

    private var mediaPlayer: MediaPlayer? = null
    private var playbackPosition = 0

    override fun preparePlayer(previewUrl: String?, callback: (Boolean) -> Unit) {
        if (previewUrl.isNullOrEmpty()) {
            callback(false)
            return
        }

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(previewUrl)
                setOnPreparedListener {
                    callback(true)
                }
                setOnErrorListener { _, _, _ ->
                    callback(false)
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            callback(false)
        }
    }

    override fun startPlayer() {
        mediaPlayer?.let {
            it.seekTo(playbackPosition)
            it.start()
        }
    }

    override fun pausePlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                playbackPosition = it.currentPosition
                it.pause()
            }
        }
    }

    override fun releasePlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        playbackPosition = 0
    }

    override fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    override fun setOnCompletionListener(listener: () -> Unit) {
        mediaPlayer?.setOnCompletionListener {
            playbackPosition = 0
            listener()
        }
    }

    override fun getFormattedTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}