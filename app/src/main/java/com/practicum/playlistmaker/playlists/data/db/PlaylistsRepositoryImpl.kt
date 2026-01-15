package com.practicum.playlistmaker.playlists.data.db

import com.practicum.playlistmaker.playlists.domain.PlaylistsRepository
import com.practicum.playlistmaker.playlists.domain.TrackAlreadyExistsException
import com.practicum.playlistmaker.playlists.domain.model.Playlist
import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class PlaylistsRepositoryImpl(
    private val playlistsDao: PlaylistsDao,
    private val playlistTracksDao: PlaylistTracksDao,
    private val playlistTrackDetailsDao: PlaylistTrackDetailsDao
) : PlaylistsRepository {

    override suspend fun createPlaylist(playlist: PlaylistEntity): Long {
        return withContext(Dispatchers.IO) {
            playlistsDao.insert(playlist)
        }
    }

    override suspend fun updatePlaylist(playlist: PlaylistEntity): Int? {
        return withContext(Dispatchers.IO) {
            playlistsDao.update(playlist)
        }
    }

    override suspend fun deletePlaylist(playlistId: Long): Int? {
        return withContext(Dispatchers.IO) {
            playlistTracksDao.deleteAllTracksFromPlaylist(playlistId)
            val result = playlistsDao.delete(playlistId)
            cleanupUnusedTracks()
            result
        }
    }

    private suspend fun getAllPlaylistsSync(): List<Playlist> {
        return withContext(Dispatchers.IO) {
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
        }
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return flow {
            val playlists = getAllPlaylistsSync()
            emit(playlists)
        }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return withContext(Dispatchers.IO) {
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
        }
    }

    override suspend fun getPlaylistsCount(): Int {
        return withContext(Dispatchers.IO) {
            playlistsDao.getCount()
        }
    }

    private suspend fun searchPlaylistsSync(query: String): List<Playlist> {
        return withContext(Dispatchers.IO) {
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
        }
    }

    override fun searchPlaylists(query: String): Flow<List<Playlist>> {
        return flow {
            val playlists = searchPlaylistsSync(query)
            emit(playlists)
        }
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {
        withContext(Dispatchers.IO) {
            val trackDetails = PlaylistTrackDetailsEntity.fromTrack(track)
            playlistTrackDetailsDao.insert(trackDetails)

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
        }
    }

    override suspend fun getTracksForPlaylist(playlistId: Long): List<Track> {
        return withContext(Dispatchers.IO) {
            val trackIds = playlistTracksDao.getTrackIdsForPlaylist(playlistId)
            val tracks = mutableListOf<Track>()

            trackIds.forEach { trackId ->
                val trackDetails = playlistTrackDetailsDao.getById(trackId)
                trackDetails?.let {
                    tracks.add(it.toTrack())
                }
            }

            tracks
        }
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Int) {
        withContext(Dispatchers.IO) {
            playlistTracksDao.deleteTrackFromPlaylist(playlistId, trackId)

            val isUsedInOtherPlaylists = isTrackUsedInAnyPlaylist(trackId)

            if (!isUsedInOtherPlaylists) {
                playlistTrackDetailsDao.delete(trackId)
            }
        }
    }

    override suspend fun getPlaylistWithTrackIds(playlistId: Long): PlaylistEntity? {
        return withContext(Dispatchers.IO) {
            playlistsDao.getById(playlistId)
        }
    }

    override suspend fun hasTrackInPlaylist(playlistId: Long, trackId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            playlistTracksDao.hasTrack(playlistId, trackId) > 0
        }
    }

    override suspend fun getTrackIdsForPlaylist(playlistId: Long): List<Int> {
        return withContext(Dispatchers.IO) {
            playlistTracksDao.getTrackIdsForPlaylist(playlistId)
        }
    }

    override suspend fun isPlaylistNameUnique(name: String): Boolean {
        return withContext(Dispatchers.IO) {
            playlistsDao.getByName(name) == null
        }
    }

    override suspend fun isTrackUsedInAnyPlaylist(trackId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val allPlaylists = playlistsDao.getAllPlaylistsSync()
            allPlaylists.any { playlist ->
                playlistTracksDao.hasTrack(playlist.playlistId, trackId) > 0
            }
        }
    }

    override suspend fun cleanupUnusedTracks() {
        withContext(Dispatchers.IO) {
            val allTracks = playlistTrackDetailsDao.getAllIds()

            allTracks.forEach { trackId ->
                val isUsedInPlaylists = isTrackUsedInAnyPlaylist(trackId)

                if (!isUsedInPlaylists) {
                    playlistTrackDetailsDao.delete(trackId)
                }
            }
        }
    }
}