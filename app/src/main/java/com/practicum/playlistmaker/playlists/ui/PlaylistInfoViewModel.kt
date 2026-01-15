package com.practicum.playlistmaker.playlists.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.favorites.data.db.FavoriteTracksDao
import com.practicum.playlistmaker.playlists.data.db.PlaylistTracksDao
import com.practicum.playlistmaker.playlists.data.db.PlaylistsDao
import com.practicum.playlistmaker.playlists.domain.PlaylistsInteractor
import com.practicum.playlistmaker.playlists.domain.model.Playlist
import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlaylistInfoViewModel @Inject constructor(
    private val playlistsDao: PlaylistsDao,
    private val playlistTracksDao: PlaylistTracksDao,
    private val favoriteTracksDao: FavoriteTracksDao,
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val _playlist = MutableLiveData<Playlist?>()
    val playlist: LiveData<Playlist?> = _playlist

    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> = _tracks

    private val _totalDuration = MutableLiveData<String>()
    val totalDuration: LiveData<String> = _totalDuration

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadPlaylistInfo(playlistId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val playlistEntity = withContext(Dispatchers.IO) {
                    playlistsDao.getById(playlistId)
                }

                playlistEntity?.let { entity ->
                    val trackCount = withContext(Dispatchers.IO) {
                        playlistTracksDao.getTrackCount(playlistId)
                    }

                    val playlist = Playlist(
                        playlistId = entity.playlistId,
                        name = entity.name,
                        description = entity.description,
                        coverPath = entity.coverPath,
                        trackCount = trackCount
                    )

                    _playlist.value = playlist

                    val tracksList = playlistsInteractor.getTracksForPlaylist(playlistId)

                    val totalDurationMillis = tracksList.sumOf { it.trackTimeMillis ?: 0L }

                    _tracks.value = tracksList

                    val totalMinutes = totalDurationMillis / 60000
                    val formattedDuration = when {
                        totalMinutes == 0L -> "0 минут"
                        totalMinutes % 10 == 1L && totalMinutes % 100 != 11L -> "$totalMinutes минута"
                        totalMinutes % 10 in 2..4 && totalMinutes % 100 !in 12..14 -> "$totalMinutes минуты"
                        else -> "$totalMinutes минут"
                    }

                    _totalDuration.value = formattedDuration

                } ?: run {
                    _playlist.value = null
                    _tracks.value = emptyList()
                    _totalDuration.value = "0 минут"
                }

            } catch (_: Exception) {
                _playlist.value = null
                _tracks.value = emptyList()
                _totalDuration.value = "0 минут"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTrackFromPlaylist(playlistId: Long, trackId: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                playlistsInteractor.removeTrackFromPlaylist(playlistId, trackId)
                loadPlaylistInfo(playlistId)
            } catch (_: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                withContext(Dispatchers.IO) {
                    playlistsInteractor.deletePlaylist(playlistId)
                }
            } catch (_: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}