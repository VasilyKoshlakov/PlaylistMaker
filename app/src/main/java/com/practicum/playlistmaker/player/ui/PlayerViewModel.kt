package com.practicum.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.practicum.playlistmaker.player.domain.PlayerInteractor
import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    private val gson: Gson
) : ViewModel() {

    fun jsonToTrack(trackJson: String): Track? {
        return try {
            gson.fromJson(trackJson, Track::class.java)
        } catch (_: Exception) {
            null
        }
    }

    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    private var progressJob: Job? = null
    private val progressUpdateInterval = 300L

    init {
        _playerState.value = PlayerState.createDefaultLoadingState()
    }

    fun preparePlayer(track: Track) {
        _playerState.value = PlayerState.createDefaultLoadingState()

        viewModelScope.launch {
            val isPrepared = playerInteractor.preparePlayer(track.previewUrl)
            if (isPrepared) {
                _playerState.postValue(PlayerState.createDefaultPreparedState())

                playerInteractor.setOnCompletionListener {
                    _playerState.postValue(
                        PlayerState.Prepared(formattedCurrentTime = Track.formatTime(0))
                    )
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

        progressJob = viewModelScope.launch {
            while (isActive && playerInteractor.isPlaying()) {
                val position = playerInteractor.getCurrentPosition()
                val formattedTime = playerInteractor.getFormattedTime(position.toLong())

                _playerState.postValue(
                    PlayerState.Playing(
                        currentPosition = position,
                        formattedCurrentTime = formattedTime
                    )
                )

                delay(progressUpdateInterval)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
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