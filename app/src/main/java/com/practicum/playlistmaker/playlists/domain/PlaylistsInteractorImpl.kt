package com.practicum.playlistmaker.playlists.domain

import android.util.Log
import com.practicum.playlistmaker.playlists.data.db.PlaylistEntity
import com.practicum.playlistmaker.playlists.domain.model.Playlist
import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PlaylistsInteractorImpl @Inject constructor(
    private val repository: PlaylistsRepository
) : PlaylistsInteractor {

    companion object {
        private const val TAG = "PlaylistsInteractor"
    }

    override suspend fun createPlaylist(
        name: String,
        description: String?,
        coverPath: String?
    ): Long {
        return try {
            val playlist = PlaylistEntity.create(
                name = name,
                description = description,
                coverPath = coverPath
            )
            repository.createPlaylist(playlist)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating playlist", e)
            -1L
        }
    }

    override suspend fun updatePlaylist(playlist: Playlist): Int? {
        return try {
            val entity = PlaylistEntity(
                playlistId = playlist.playlistId,
                name = playlist.name,
                description = playlist.description,
                coverPath = playlist.coverPath
            )
            repository.updatePlaylist(entity)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating playlist", e)
            null
        }
    }

    override suspend fun deletePlaylist(playlistId: Long): Int? {
        return try {
            repository.deletePlaylist(playlistId)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting playlist", e)
            null
        }
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return flow {
            try {
                val playlists = repository.getAllPlaylists()
                playlists.collect {
                    emit(it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting all playlists", e)
                emit(emptyList())
            }
        }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return try {
            repository.getPlaylistById(playlistId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting playlist by id", e)
            null
        }
    }

    override suspend fun getPlaylistsCount(): Int {
        return try {
            repository.getPlaylistsCount()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting playlists count", e)
            0
        }
    }

    override fun searchPlaylists(query: String): Flow<List<Playlist>> {
        return flow {
            try {
                val playlists = repository.searchPlaylists(query)
                playlists.collect {
                    emit(it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error searching playlists", e)
                emit(emptyList())
            }
        }
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track): Boolean {
        Log.d(TAG, "addTrackToPlaylist interactor - playlistId: $playlistId, trackId: ${track.trackId}")
        return try {
            repository.addTrackToPlaylist(playlistId, track)
            Log.d(TAG, "Repository call completed")
            true
        } catch (_: TrackAlreadyExistsException) {
            Log.d(TAG, "Track already exists in playlist")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error adding track to playlist in interactor", e)
            false
        }
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Int) {
        try {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing track from playlist", e)
            throw e
        }
    }

    override suspend fun getPlaylistWithTrackIds(playlistId: Long): PlaylistEntity? {
        return try {
            repository.getPlaylistWithTrackIds(playlistId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting playlist with track ids", e)
            null
        }
    }

    override suspend fun hasTrackInPlaylist(playlistId: Long, trackId: Int): Boolean {
        return try {
            repository.hasTrackInPlaylist(playlistId, trackId)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if track in playlist", e)
            false
        }
    }

    override suspend fun getTrackIdsForPlaylist(playlistId: Long): List<Int> {
        return try {
            repository.getTrackIdsForPlaylist(playlistId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting track ids for playlist", e)
            emptyList()
        }
    }

    override suspend fun isPlaylistNameUnique(name: String): Boolean {
        return try {
            repository.isPlaylistNameUnique(name)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking playlist name uniqueness", e)
            true
        }
    }
}