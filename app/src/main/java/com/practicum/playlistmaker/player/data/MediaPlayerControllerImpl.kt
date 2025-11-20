package com.practicum.playlistmaker.player.data

import android.media.MediaPlayer
import com.practicum.playlistmaker.player.domain.MediaPlayerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class MediaPlayerControllerImpl(
    private val mediaPlayerFactory: MediaPlayerFactory
) : MediaPlayerController {
    private var mediaPlayer: MediaPlayer? = null
    private var playbackPosition = 0

    override suspend fun preparePlayer(previewUrl: String?): Boolean = withContext(Dispatchers.IO) {
        if (previewUrl.isNullOrEmpty()) {
            return@withContext false
        }

        try {
            mediaPlayer?.release()
            mediaPlayer = mediaPlayerFactory.createMediaPlayer().apply {
                setDataSource(previewUrl)
                prepare()
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
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