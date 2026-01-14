package com.practicum.playlistmaker.playlists.domain

import com.practicum.playlistmaker.favorites.data.db.FavoriteTracksDao
import com.practicum.playlistmaker.playlists.data.db.PlaylistEntity
import com.practicum.playlistmaker.playlists.domain.model.Playlist
import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlaylistsInteractorImpl @Inject constructor(
    private val repository: PlaylistsRepository,
    private val favoriteTracksDao: FavoriteTracksDao
) : PlaylistsInteractor {

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track): Boolean {
        return try {
            val hasTrack = repository.hasTrackInPlaylist(playlistId, track.trackId)

            if (!hasTrack) {
                repository.addTrackToPlaylist(playlistId, track)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
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
        } catch (_: Exception) {
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
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun deletePlaylist(playlistId: Long): Int? {
        return try {
            repository.deletePlaylist(playlistId)
        } catch (_: Exception) {
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
            } catch (_: Exception) {
                emit(emptyList())
            }
        }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return try {
            repository.getPlaylistById(playlistId)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getPlaylistsCount(): Int {
        return try {
            repository.getPlaylistsCount()
        } catch (_: Exception) {
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
            } catch (_: Exception) {
                emit(emptyList())
            }
        }
    }

    override suspend fun getPlaylistWithTrackIds(playlistId: Long): PlaylistEntity? {
        return try {
            repository.getPlaylistWithTrackIds(playlistId)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun hasTrackInPlaylist(playlistId: Long, trackId: Int): Boolean {
        return try {
            repository.hasTrackInPlaylist(playlistId, trackId)
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun getTrackIdsForPlaylist(playlistId: Long): List<Int> {
        return try {
            repository.getTrackIdsForPlaylist(playlistId)
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun isPlaylistNameUnique(name: String): Boolean {
        return try {
            repository.isPlaylistNameUnique(name)
        } catch (_: Exception) {
            true
        }
    }

    override suspend fun getTracksForPlaylist(playlistId: Long): List<Track> {
        return withContext(Dispatchers.IO) {
            try {
                repository.getTracksForPlaylist(playlistId)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Int) {
        try {
            withContext(Dispatchers.IO) {
                repository.removeTrackFromPlaylist(playlistId, trackId)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun cleanupUnusedTracks() {
        withContext(Dispatchers.IO) {
            try {
                repository.cleanupUnusedTracks()
            } catch (_: Exception) {
            }
        }
    }
}