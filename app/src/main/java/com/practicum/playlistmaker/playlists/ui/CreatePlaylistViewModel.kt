package com.practicum.playlistmaker.playlists.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.playlists.domain.PlaylistsInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreatePlaylistViewModel @Inject constructor(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val _isCreateButtonEnabled = MutableLiveData<Boolean>()
    val isCreateButtonEnabled: LiveData<Boolean> = _isCreateButtonEnabled

    private var title = ""
    private var description = ""

    fun updateTitle(newTitle: String) {
        title = newTitle.trim()
        updateCreateButtonState()
    }

    fun updateDescription(newDescription: String) {
        description = newDescription.trim()
    }

    fun hasChanges(): Boolean {
        return title.isNotEmpty() || description.isNotEmpty()
    }

    suspend fun createPlaylist(
        name: String,
        description: String?,
        coverPath: String?
    ): Long {
        return withContext(Dispatchers.IO) {
            playlistsInteractor.createPlaylist(name, description, coverPath)
        }
    }

    suspend fun isPlaylistNameUnique(name: String): Boolean {
        return withContext(Dispatchers.IO) {
            playlistsInteractor.isPlaylistNameUnique(name)
        }
    }

    private fun updateCreateButtonState() {
        _isCreateButtonEnabled.value = title.isNotEmpty()
    }
}