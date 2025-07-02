package com.practicum.playlistmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val trackName: TextView = itemView.findViewById(R.id.track_name)
    private val artistName: TextView = itemView.findViewById(R.id.artist_name)
    private val trackTime: TextView = itemView.findViewById(R.id.track_time)
    private val artwork: ImageView = itemView.findViewById(R.id.artwork)

    fun bind(track: Track) {
        trackName.text = track.trackName
        artistName.text = track.artistName
        trackTime.text = formatTrackTime(track.trackTimeMillis)

        loadArtwork(track.artworkUrl100)
    }

    private fun formatTrackTime(millis: Long?): String {
        return if (millis != null) {
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(millis)
        } else {
            ""
        }
    }

    private fun loadArtwork(url: String?) {
        val radius = itemView.context.resources
            .getDimensionPixelSize(R.dimen.artwork_corner_radius)

        Glide.with(itemView)
            .load(url)
            .placeholder(R.drawable.placeholder_2)
            .error(R.drawable.placeholder_2)
            .transform(RoundedCorners(radius))
            .into(artwork)
    }

    companion object {
        fun create(parent: ViewGroup): TrackViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_track, parent, false)
            return TrackViewHolder(view)
        }
    }
}
