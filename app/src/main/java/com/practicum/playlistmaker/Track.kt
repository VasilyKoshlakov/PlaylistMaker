package com.practicum.playlistmaker

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Locale

@Parcelize
data class Track(
    val trackId: Int,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?
) : Parcelable {

    fun getCoverArtwork() = artworkUrl100?.replaceAfterLast('/', "512x512bb.jpg")

    fun getFormattedTrackTime(): String {
        return if (trackTimeMillis != null) {
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(trackTimeMillis)
        } else {
            ""
        }
    }

    fun getReleaseYear(): String? {
        return releaseDate?.take(4)
    }
}
