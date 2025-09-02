package com.practicum.playlistmaker.data.dto

data class ApiResponseDto(
    val resultCount: Int,
    val results: List<TrackDto>
)
