package com.practicum.playlistmaker.playlists.data.db

import com.practicum.playlistmaker.playlists.domain.PlaylistsRepository
import com.practicum.playlistmaker.playlists.domain.TrackAlreadyExistsException
import com.practicum.playlistmaker.playlists.domain.model.Playlist
import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlaylistsRepositoryImpl @Inject constructor(
    private val playlistsDao: PlaylistsDao,
    private val playlistTracksDao: PlaylistTracksDao
) : PlaylistsRepository {

    override suspend fun createPlaylist(playlist: PlaylistEntity): Long {
        return withContext(Dispatchers.IO) {
            try {
                playlistsDao.insert(playlist)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun updatePlaylist(playlist: PlaylistEntity): Int? {
        return withContext(Dispatchers.IO) {
            try {
                playlistsDao.update(playlist)
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun deletePlaylist(playlistId: Long): Int? {
        return withContext(Dispatchers.IO) {
            try {
                playlistTracksDao.deleteAllTracksFromPlaylist(playlistId)
                playlistsDao.delete(playlistId)
            } catch (_: Exception) {
                null
            }
        }
    }

    private suspend fun getAllPlaylistsSync(): List<Playlist> {
        return withContext(Dispatchers.IO) {
            try {
                val playlists = playlistsDao.getAllPlaylistsSync()
                playlists.map { playlistEntity ->
                    val trackCount = playlistTracksDao.getTrackCount(playlistEntity.playlistId)
                    Playlist(
                        playlistId = playlistEntity.playlistId,
                        name = playlistEntity.name,
                        description = playlistEntity.description,
                        coverPath = playlistEntity.coverPath,
                        trackCount = trackCount
                    )
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return flow {
            try {
                val playlists = getAllPlaylistsSync()
                emit(playlists)
            } catch (_: Exception) {
                emit(emptyList())
            }
        }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return withContext(Dispatchers.IO) {
            try {
                val playlistEntity = playlistsDao.getById(playlistId)
                playlistEntity?.let { entity ->
                    val trackCount = playlistTracksDao.getTrackCount(playlistId)
                    Playlist(
                        playlistId = entity.playlistId,
                        name = entity.name,
                        description = entity.description,
                        coverPath = entity.coverPath,
                        trackCount = trackCount
                    )
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun getPlaylistsCount(): Int {
        return withContext(Dispatchers.IO) {
            try {
                playlistsDao.getCount()
            } catch (_: Exception) {
                0
            }
        }
    }

    private suspend fun searchPlaylistsSync(query: String): List<Playlist> {
        return withContext(Dispatchers.IO) {
            try {
                val playlists = playlistsDao.searchPlaylistsSync(query)
                playlists.map { playlistEntity ->
                    val trackCount = playlistTracksDao.getTrackCount(playlistEntity.playlistId)
                    Playlist(
                        playlistId = playlistEntity.playlistId,
                        name = playlistEntity.name,
                        description = playlistEntity.description,
                        coverPath = playlistEntity.coverPath,
                        trackCount = trackCount
                    )
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override fun searchPlaylists(query: String): Flow<List<Playlist>> {
        return flow {
            try {
                val playlists = searchPlaylistsSync(query)
                emit(playlists)
            } catch (_: Exception) {
                emit(emptyList())
            }
        }
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {
        withContext(Dispatchers.IO) {
            try {
                val alreadyExists = playlistTracksDao.hasTrack(playlistId, track.trackId) > 0

                if (!alreadyExists) {
                    playlistTracksDao.insert(
                        PlaylistTrackEntity(
                            playlistId = playlistId,
                            trackId = track.trackId
                        )
                    )
                } else {
                    throw TrackAlreadyExistsException("Трек уже существует в плейлисте")
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Int) {
        withContext(Dispatchers.IO) {
            try {
                playlistTracksDao.deleteTrackFromPlaylist(playlistId, trackId)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun getPlaylistWithTrackIds(playlistId: Long): PlaylistEntity? {
        return withContext(Dispatchers.IO) {
            try {
                playlistsDao.getById(playlistId)
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun hasTrackInPlaylist(playlistId: Long, trackId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                playlistTracksDao.hasTrack(playlistId, trackId) > 0
            } catch (_: Exception) {
                false
            }
        }
    }

    override suspend fun getTrackIdsForPlaylist(playlistId: Long): List<Int> {
        return withContext(Dispatchers.IO) {
            try {
                playlistTracksDao.getTrackIdsForPlaylist(playlistId)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun isPlaylistNameUnique(name: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                playlistsDao.getByName(name) == null
            } catch (_: Exception) {
                true
            }
        }
    }
}