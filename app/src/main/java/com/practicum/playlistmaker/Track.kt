package com.practicum.playlistmaker

import java.text.SimpleDateFormat
import java.util.Locale

data class Track(
    val trackId: Int,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?
)
