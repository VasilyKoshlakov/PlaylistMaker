package com.practicum.playlistmaker.playlists.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val playlistId: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "cover_path")
    val coverPath: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun create(
            name: String,
            description: String? = null,
            coverPath: String? = null
        ): PlaylistEntity {
            return PlaylistEntity(
                name = name,
                description = description,
                coverPath = coverPath
            )
        }
    }
}