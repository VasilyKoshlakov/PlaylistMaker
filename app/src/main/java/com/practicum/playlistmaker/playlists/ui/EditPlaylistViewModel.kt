package com.practicum.playlistmaker.playlists.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.playlists.domain.PlaylistsInteractor
import com.practicum.playlistmaker.playlists.domain.model.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EditPlaylistViewModel @Inject constructor(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val _isSaveButtonEnabled = MutableLiveData<Boolean>()
    val isSaveButtonEnabled: LiveData<Boolean> = _isSaveButtonEnabled

    private var playlist: Playlist? = null
    private var title = ""
    private var description = ""
    private var hasChanges = false

    suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return withContext(Dispatchers.IO) {
            playlistsInteractor.getPlaylistById(playlistId)
        }
    }

    fun initialize(playlist: Playlist) {
        this.playlist = playlist
        this.title = playlist.name
        this.description = playlist.description ?: ""
        _isSaveButtonEnabled.value = title.isNotEmpty()
    }

    fun updateTitle(newTitle: String) {
        title = newTitle.trim()
        updateSaveButtonState()
        checkForChanges()
    }

    fun updateDescription(newDescription: String) {
        description = newDescription.trim()
        checkForChanges()
    }

    suspend fun savePlaylist(coverPath: String?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val playlistToSave = Playlist(
                    playlistId = playlist?.playlistId ?: 0,
                    name = title,
                    description = description.ifEmpty { null },
                    coverPath = coverPath,
                    trackCount = playlist?.trackCount ?: 0
                )

                val result = playlistsInteractor.updatePlaylist(playlistToSave)
                result != null && result > 0
            } catch (_: Exception) {
                false
            }
        }
    }

    fun getCurrentCoverPath(): String? = playlist?.coverPath

    private fun updateSaveButtonState() {
        _isSaveButtonEnabled.value = title.isNotEmpty()
    }

    private fun checkForChanges() {
        hasChanges = title != playlist?.name ||
                description != (playlist?.description ?: "") ||
                (playlist?.coverPath == null && title.isNotEmpty())
    }
}