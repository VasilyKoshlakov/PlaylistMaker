package com.practicum.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.practicum.playlistmaker.favorites.domain.FavoritesInteractor
import com.practicum.playlistmaker.playlists.domain.PlaylistsInteractor
import com.practicum.playlistmaker.player.domain.PlayerInteractor
import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private val _addToPlaylistResult = MutableLiveData<AddToPlaylistResult>()
    val addToPlaylistResult: LiveData<AddToPlaylistResult> = _addToPlaylistResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentTrack: Track? = null
    private var progressJob: Job? = null
    private var favoriteJob: Job? = null
    private val progressUpdateInterval = 300L

    init {
        _playerState.value = PlayerState.createDefaultLoadingState()
    }

    fun preparePlayer(track: Track) {
        currentTrack = track

        _playerState.value = PlayerState.createDefaultLoadingState()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isPrepared = playerInteractor.preparePlayer(track.previewUrl)
                if (isPrepared) {
                    withContext(Dispatchers.Main) {
                        _playerState.value = PlayerState.createDefaultPreparedState()
                    }

                    playerInteractor.setOnCompletionListener {
                        viewModelScope.launch(Dispatchers.Main) {
                            _playerState.value = PlayerState.Prepared(formattedCurrentTime = Track.formatTime(0))
                            stopProgressUpdates()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _playerState.value = PlayerState.Error()
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _playerState.value = PlayerState.Error()
                }
            }
        }

        loadFavoriteState(track.trackId)
    }

    private fun loadFavoriteState(trackId: Int) {
        favoriteJob?.cancel()
        favoriteJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                favoritesInteractor.isFavorite(trackId).collect { isFavorite ->
                    withContext(Dispatchers.Main) {
                        _isFavorite.value = isFavorite
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun toggleFavorite() {
        currentTrack?.let { track ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    favoritesInteractor.toggleFavorite(track)
                } catch (_: Exception) {
                }
            }
        }
    }

    fun togglePlayback() {
        val isPlaying = playerInteractor.isPlaying()

        if (isPlaying) {
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

        progressJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    if (!playerInteractor.isPlaying()) {
                        break
                    }

                    val position = playerInteractor.getCurrentPosition()
                    val formattedTime = playerInteractor.getFormattedTime(position.toLong())

                    withContext(Dispatchers.Main) {
                        _playerState.value = PlayerState.Playing(
                            currentPosition = position,
                            formattedCurrentTime = formattedTime
                        )
                    }

                    delay(progressUpdateInterval)
                } catch (_: Exception) {
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
        playerInteractor.releasePlayer()
        _playerState.value = PlayerState.createDefaultLoadingState()
    }

    fun loadPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    _isLoading.value = true
                }

                playlistsInteractor.getAllPlaylists().collect { playlists ->
                    withContext(Dispatchers.Main) {
                        _playlists.value = playlists
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _playlists.value = emptyList()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun addTrackToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    _isLoading.value = true
                }

                val playlist = withContext(Dispatchers.IO) {
                    playlistsInteractor.getPlaylistById(playlistId)
                }

                if (playlist == null) {
                    _addToPlaylistResult.value = AddToPlaylistResult.Error("Плейлист не найден")
                    return@launch
                }

                val success = withContext(Dispatchers.IO) {
                    try {
                        playlistsInteractor.addTrackToPlaylist(playlistId, track)
                    } catch (_: Exception) {
                        false
                    }
                }

                if (success) {
                    _addToPlaylistResult.value = AddToPlaylistResult.Success(playlist.name)

                    viewModelScope.launch(Dispatchers.IO) {
                        delay(500)
                        loadPlaylists()
                    }
                } else {
                    _addToPlaylistResult.value = AddToPlaylistResult.AlreadyExists(playlist.name)
                    viewModelScope.launch(Dispatchers.IO) {
                        delay(500)
                        loadPlaylists()
                    }
                }

            } catch (_: Exception) {
                _addToPlaylistResult.value = AddToPlaylistResult.Error("Ошибка при добавлении")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetAddToPlaylistResult() {
        _addToPlaylistResult.value = AddToPlaylistResult.None
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