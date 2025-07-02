package com.practicum.playlistmaker

data class ApiResponse(
    val resultCount: Int,
    val results: List<Track>
)
