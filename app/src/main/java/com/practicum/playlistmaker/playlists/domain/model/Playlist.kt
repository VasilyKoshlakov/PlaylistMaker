package com.practicum.playlistmaker.playlists.domain.model

data class Playlist(
    val playlistId: Long,
    val name: String,
    val description: String? = null,
    val coverPath: String? = null,
    val trackCount: Int = 0
)