package com.practicum.playlistmaker.playlists.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlaylistTrackDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: PlaylistTrackDetailsEntity)

    @Query("DELETE FROM playlist_track_details WHERE trackId = :trackId")
    suspend fun delete(trackId: Int)

    @Query("SELECT * FROM playlist_track_details WHERE trackId = :trackId LIMIT 1")
    suspend fun getById(trackId: Int): PlaylistTrackDetailsEntity?

    @Query("SELECT trackId FROM playlist_track_details")
    suspend fun getAllIds(): List<Int>

    @Query("SELECT COUNT(*) FROM playlist_track_details WHERE trackId = :trackId")
    suspend fun hasTrack(trackId: Int): Int
}