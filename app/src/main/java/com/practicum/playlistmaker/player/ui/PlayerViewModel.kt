package com.practicum.playlistmaker.player.ui

import com.practicum.playlistmaker.player.domain.Track
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.player.domain.PlayerInteractor
import java.util.Timer
import java.util.TimerTask

class PlayerViewModel(private val playerInteractor: PlayerInteractor) : ViewModel() {

    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    private var progressTimer: Timer? = null
    private val progressUpdateInterval = 500L

    init {
        _playerState.value = PlayerState.createDefaultLoadingState()
    }

    fun preparePlayer(track: Track) {
        _playerState.value = PlayerState.createDefaultLoadingState()

        playerInteractor.preparePlayer(track.previewUrl) { isPrepared ->
            if (isPrepared) {
                _playerState.postValue(PlayerState.createDefaultPreparedState())

                playerInteractor.setOnCompletionListener {
                    _playerState.postValue(PlayerState.createDefaultPreparedState())
                    stopProgressUpdates()
                }
            } else {
                _playerState.postValue(PlayerState.Error())
            }
        }
    }

    fun togglePlayback() {
        if (playerInteractor.isPlaying()) {
            pausePlayer()
        } else {
            startPlayer()
        }
    }

    private fun startPlayer() {
        playerInteractor.startPlayer()
        startProgressUpdates()
    }

    private fun pausePlayer() {
        playerInteractor.pausePlayer()
        stopProgressUpdates()

        when (val currentState = _playerState.value) {
            is PlayerState.Playing -> {
                _playerState.value = PlayerState.Paused(
                    currentPosition = currentState.currentPosition,
                    formattedCurrentTime = currentState.formattedCurrentTime
                )
            }
            else -> { }
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()

        progressTimer = Timer()
        progressTimer?.schedule(object : TimerTask() {
            override fun run() {
                val position = playerInteractor.getCurrentPosition()
                val isPlaying = playerInteractor.isPlaying()
                val formattedTime = playerInteractor.getFormattedTime(position.toLong())

                if (isPlaying) {
                    _playerState.postValue(
                        PlayerState.Playing(
                            currentPosition = position,
                            formattedCurrentTime = formattedTime
                        )
                    )
                }
            }
        }, 0, progressUpdateInterval)
    }

    private fun stopProgressUpdates() {
        progressTimer?.cancel()
        progressTimer = null
    }

    fun releasePlayer() {
        stopProgressUpdates()
        playerInteractor.releasePlayer()
        _playerState.value = PlayerState.createDefaultLoadingState()
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}

