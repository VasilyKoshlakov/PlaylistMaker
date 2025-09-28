package com.practicum.playlistmaker.player.data

import android.media.MediaPlayer
import java.util.Locale

class MediaPlayerController {
    private var mediaPlayer: MediaPlayer? = null
    private var playbackPosition = 0

    fun preparePlayer(previewUrl: String?, callback: (Boolean) -> Unit) {
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

    fun startPlayer() {
        mediaPlayer?.let {
            it.seekTo(playbackPosition)
            it.start()
        }
    }

    fun pausePlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                playbackPosition = it.currentPosition
                it.pause()
            }
        }
    }

    fun releasePlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        playbackPosition = 0
    }

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun setOnCompletionListener(listener: () -> Unit) {
        mediaPlayer?.setOnCompletionListener {
            playbackPosition = 0
            listener()
        }
    }

    fun getFormattedTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}