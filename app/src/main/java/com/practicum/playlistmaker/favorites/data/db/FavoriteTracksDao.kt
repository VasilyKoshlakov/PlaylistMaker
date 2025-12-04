package com.practicum.playlistmaker.favorites.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTracksDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: FavoriteTrackEntity)

    @Query("DELETE FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun delete(trackId: Int)

    @Query("SELECT * FROM favorite_tracks ORDER BY addedAt DESC")
    fun getAll(): Flow<List<FavoriteTrackEntity>>

    @Query("SELECT trackId FROM favorite_tracks")
    suspend fun getAllIds(): List<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tracks WHERE trackId = :trackId LIMIT 1)")
    fun isFavorite(trackId: Int): Flow<Boolean>
}
