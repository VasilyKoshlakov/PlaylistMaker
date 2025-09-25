package com.practicum.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.player.domain.PlayerInteractor
import com.practicum.playlistmaker.player.domain.Track
import java.util.Timer
import java.util.TimerTask

class PlayerViewModel(private val playerInteractor: PlayerInteractor) : ViewModel() {

    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    private var progressTimer: Timer? = null
    private val progressUpdateInterval = 500L

    init {
        _playerState.value = PlayerState(
            isPlaying = false,
            currentPosition = 0,
            isPrepared = false,
            formattedCurrentTime = "00:00"
        )
    }

    fun preparePlayer(track: Track) {
        playerInteractor.preparePlayer(track.previewUrl) { isPrepared ->
            _playerState.postValue(
                _playerState.value?.copy(isPrepared = isPrepared) ?: PlayerState(
                    isPlaying = false,
                    currentPosition = 0,
                    isPrepared = isPrepared,
                    formattedCurrentTime = "00:00"
                )
            )
        }

        playerInteractor.setOnCompletionListener {
            _playerState.postValue(
                _playerState.value?.copy(
                    isPlaying = false,
                    currentPosition = 0,
                    formattedCurrentTime = "00:00"
                ) ?: PlayerState(
                    isPlaying = false,
                    currentPosition = 0,
                    isPrepared = true,
                    formattedCurrentTime = "00:00"
                )
            )
            stopProgressUpdates()
        }
    }

    fun togglePlayback() {
        val currentState = _playerState.value
        if (currentState?.isPlaying == true) {
            pausePlayer()
        } else {
            startPlayer()
        }
    }

    private fun startPlayer() {
        playerInteractor.startPlayer()
        _playerState.value = _playerState.value?.copy(isPlaying = true)
        startProgressUpdates()
    }

    private fun pausePlayer() {
        playerInteractor.pausePlayer()
        _playerState.value = _playerState.value?.copy(isPlaying = false)
        stopProgressUpdates()
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()

        progressTimer = Timer()
        progressTimer?.schedule(object : TimerTask() {
            override fun run() {
                if (_playerState.value?.isPlaying == true) {
                    val position = playerInteractor.getCurrentPosition()
                    val formattedTime = playerInteractor.getFormattedTime(position.toLong())
                    _playerState.postValue(
                        _playerState.value?.copy(
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
        _playerState.value = PlayerState(
            isPlaying = false,
            currentPosition = 0,
            isPrepared = false,
            formattedCurrentTime = "00:00"
        )
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}

