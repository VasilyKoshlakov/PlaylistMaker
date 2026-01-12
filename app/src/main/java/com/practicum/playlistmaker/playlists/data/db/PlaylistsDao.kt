package com.practicum.playlistmaker.playlists.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistEntity): Long

    @Update
    suspend fun update(playlist: PlaylistEntity): Int

    @Query("DELETE FROM playlists WHERE playlistId = :playlistId")
    suspend fun delete(playlistId: Long): Int

    @Query("SELECT * FROM playlists ORDER BY created_at DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists ORDER BY created_at DESC")
    suspend fun getAllPlaylistsSync(): List<PlaylistEntity>

    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId")
    suspend fun getById(playlistId: Long): PlaylistEntity?

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getCount(): Int

    @Query("SELECT * FROM playlists WHERE name LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun searchPlaylists(query: String): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE name LIKE '%' || :query || '%' ORDER BY created_at DESC")
    suspend fun searchPlaylistsSync(query: String): List<PlaylistEntity>

    @Query("SELECT * FROM playlists WHERE name = :name")
    suspend fun getByName(name: String): PlaylistEntity?
}