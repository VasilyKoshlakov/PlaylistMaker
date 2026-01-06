package com.practicum.playlistmaker.player.data

import android.media.MediaPlayer
import com.practicum.playlistmaker.player.domain.MediaPlayerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class MediaPlayerControllerImpl(
    private val mediaPlayerFactory: MediaPlayerFactory
) : MediaPlayerController {

    private var mediaPlayer: MediaPlayer? = null
    private var playbackPosition = 0
    private var isPrepared = false

    companion object {
        private const val PREPARE_TIMEOUT_MS = 5000L
    }

    override suspend fun preparePlayer(previewUrl: String?): Boolean {
        if (previewUrl.isNullOrEmpty()) {
            return false
        }

        releasePlayer()

        return withContext(Dispatchers.IO) {
            try {
                val newMediaPlayer = mediaPlayerFactory.createMediaPlayer()
                var preparationResult: Boolean

                val latch = CountDownLatch(1)
                val preparedFlag = AtomicBoolean(false)
                val errorFlag = AtomicBoolean(false)

                newMediaPlayer.setOnPreparedListener {
                    preparedFlag.set(true)
                    isPrepared = true
                    latch.countDown()
                }

                newMediaPlayer.setOnErrorListener { _, what, extra ->
                    errorFlag.set(true)
                    latch.countDown()
                    false
                }

                try {
                    newMediaPlayer.setDataSource(previewUrl)
                    newMediaPlayer.prepareAsync()

                    val completed = latch.await(PREPARE_TIMEOUT_MS, TimeUnit.MILLISECONDS)

                    if (completed && preparedFlag.get() && !errorFlag.get()) {
                        mediaPlayer = newMediaPlayer
                        preparationResult = true
                    } else {
                        newMediaPlayer.release()
                        preparationResult = false
                    }

                } catch (_: Exception) {
                    newMediaPlayer.release()
                    preparationResult = false
                }

                preparationResult

            } catch (_: Exception) {
                false
            }
        }
    }

    override fun startPlayer() {
        if (isPrepared) {
            mediaPlayer?.let {
                try {
                    it.seekTo(playbackPosition)
                    it.start()
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun pausePlayer() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    playbackPosition = it.currentPosition
                    it.pause()
                }
            } catch (_: Exception) {
            }
        }
    }

    override fun releasePlayer() {
        mediaPlayer?.let {
            try {
                it.setOnPreparedListener(null)
                it.setOnErrorListener(null)
                it.setOnCompletionListener(null)

                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (_: Exception) {
            }
        }
        mediaPlayer = null
        playbackPosition = 0
        isPrepared = false
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