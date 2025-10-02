package com.practicum.playlistmaker.search.data

data class ApiResponseDto(
    val resultCount: Int,
    val results: List<TrackDto>
)