package com.practicum.playlistmaker.playlists.domain

import com.practicum.playlistmaker.playlists.data.db.PlaylistEntity
import com.practicum.playlistmaker.playlists.domain.model.Playlist
import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsRepository {
    suspend fun createPlaylist(playlist: PlaylistEntity): Long
    suspend fun updatePlaylist(playlist: PlaylistEntity): Int?
    suspend fun deletePlaylist(playlistId: Long): Int?
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    suspend fun getPlaylistsCount(): Int
    fun searchPlaylists(query: String): Flow<List<Playlist>>

    suspend fun addTrackToPlaylist(playlistId: Long, track: Track)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Int)
    suspend fun getPlaylistWithTrackIds(playlistId: Long): PlaylistEntity?
    suspend fun hasTrackInPlaylist(playlistId: Long, trackId: Int): Boolean
    suspend fun getTrackIdsForPlaylist(playlistId: Long): List<Int>
    suspend fun isPlaylistNameUnique(name: String): Boolean
}