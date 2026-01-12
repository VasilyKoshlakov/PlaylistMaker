package com.practicum.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.practicum.playlistmaker.common.SingleLiveEvent
import com.practicum.playlistmaker.favorites.domain.FavoritesInteractor
import com.practicum.playlistmaker.playlists.domain.PlaylistsInteractor
import com.practicum.playlistmaker.player.domain.PlayerInteractor
import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlayerViewModel @Inject constructor(
    private val playerInteractor: PlayerInteractor,
    private val favoritesInteractor: FavoritesInteractor,
    private val playlistsInteractor: PlaylistsInteractor,
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

    private val _isFavorite = MutableLiveData(false)
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _playlists = MutableLiveData<List<com.practicum.playlistmaker.playlists.domain.model.Playlist>>(emptyList())
    val playlists: LiveData<List<com.practicum.playlistmaker.playlists.domain.model.Playlist>> = _playlists

    private val _addToPlaylistResult = SingleLiveEvent<AddToPlaylistResult>()
    val addToPlaylistResult: LiveData<AddToPlaylistResult> = _addToPlaylistResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentTrack: Track? = null
    private var progressJob: Job? = null
    private var favoriteJob: Job? = null
    private var isPlaying = false
    private val progressUpdateInterval = 300L

    init {
        _playerState.value = PlayerState.createDefaultLoadingState()
    }

    fun preparePlayer(track: Track) {
        currentTrack = track

        _playerState.value = PlayerState.Prepared(
            formattedCurrentTime = Track.formatTime(0)
        )

        loadFavoriteState(track.trackId)

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val isPrepared = withContext(Dispatchers.IO) {
                    playerInteractor.preparePlayer(track.previewUrl)
                }

                if (isPrepared) {
                    playerInteractor.setOnCompletionListener {
                        viewModelScope.launch {
                            isPlaying = false
                            _playerState.value = PlayerState.Prepared(
                                formattedCurrentTime = Track.formatTime(0)
                            )
                            stopProgressUpdates()
                        }
                    }

                    _playerState.value = PlayerState.Prepared(
                        formattedCurrentTime = Track.formatTime(0)
                    )
                } else {
                    _playerState.value = PlayerState.Prepared(
                        formattedCurrentTime = Track.formatTime(0)
                    )
                }
            } catch (_: Exception) {
                _playerState.value = PlayerState.Prepared(
                    formattedCurrentTime = Track.formatTime(0)
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadFavoriteState(trackId: Int) {
        favoriteJob?.cancel()
        favoriteJob = viewModelScope.launch {
            try {
                favoritesInteractor.isFavorite(trackId).collect { isFavorite ->
                    _isFavorite.value = isFavorite
                }
            } catch (_: Exception) {
            }
        }
    }

    fun toggleFavorite() {
        currentTrack?.let { track ->
            viewModelScope.launch {
                try {
                    favoritesInteractor.toggleFavorite(track)
                } catch (_: Exception) {
                }
            }
        }
    }

    fun togglePlayback() {
        val wasPlaying = isPlaying

        if (wasPlaying) {
            playerInteractor.pausePlayer()
            isPlaying = false
            stopProgressUpdates()

            when (val currentState = _playerState.value) {
                is PlayerState.Playing -> {
                    _playerState.value = PlayerState.Paused(
                        currentPosition = currentState.currentPosition,
                        formattedCurrentTime = currentState.formattedCurrentTime
                    )
                }
                else -> {
                    _playerState.value = PlayerState.Prepared(
                        formattedCurrentTime = Track.formatTime(0)
                    )
                }
            }
        } else {
            playerInteractor.startPlayer()
            isPlaying = true
            startProgressUpdates()

            when (val currentState = _playerState.value) {
                is PlayerState.Prepared -> {
                    _playerState.value = PlayerState.Playing(
                        currentPosition = 0,
                        formattedCurrentTime = Track.formatTime(0)
                    )
                }
                is PlayerState.Paused -> {
                    _playerState.value = PlayerState.Playing(
                        currentPosition = currentState.currentPosition,
                        formattedCurrentTime = currentState.formattedCurrentTime
                    )
                }
                else -> {
                    _playerState.value = PlayerState.Playing(
                        currentPosition = 0,
                        formattedCurrentTime = Track.formatTime(0)
                    )
                }
            }
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()

        progressJob = viewModelScope.launch {
            while (isActive) {
                try {
                    if (!isPlaying) {
                        break
                    }

                    val position = withContext(Dispatchers.IO) {
                        playerInteractor.getCurrentPosition()
                    }

                    val formattedTime = withContext(Dispatchers.IO) {
                        playerInteractor.getFormattedTime(position.toLong())
                    }

                    _playerState.value = PlayerState.Playing(
                        currentPosition = position,
                        formattedCurrentTime = formattedTime
                    )

                    delay(progressUpdateInterval)
                } catch (_: Exception) {
                    break
                }
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    fun releasePlayer() {
        stopProgressUpdates()
        favoriteJob?.cancel()
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    playerInteractor.releasePlayer()
                }
            } catch (_: Exception) {
            }
        }
        _playerState.value = PlayerState.createDefaultLoadingState()
        isPlaying = false
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                playlistsInteractor.getAllPlaylists().collect { playlists ->
                    _playlists.value = playlists
                }
            } catch (_: Exception) {
                _playlists.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTrackToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val playlist = withContext(Dispatchers.IO) {
                    playlistsInteractor.getPlaylistById(playlistId)
                }

                if (playlist == null) {
                    _addToPlaylistResult.value = AddToPlaylistResult.Error("Плейлист не найден")
                    return@launch
                }

                val success = withContext(Dispatchers.IO) {
                    playlistsInteractor.addTrackToPlaylist(playlistId, track)
                }

                if (success) {
                    _addToPlaylistResult.value = AddToPlaylistResult.Success(playlist.name)

                    loadPlaylists()
                } else {
                    _addToPlaylistResult.value = AddToPlaylistResult.AlreadyExists(playlist.name)
                }
            } catch (_: Exception) {
                _addToPlaylistResult.value = AddToPlaylistResult.Error("Ошибка при добавлении")
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}

sealed class AddToPlaylistResult {
    object None : AddToPlaylistResult()
    data class Success(val playlistName: String) : AddToPlaylistResult()
    data class AlreadyExists(val playlistName: String) : AddToPlaylistResult()
    data class Error(val message: String) : AddToPlaylistResult()
}