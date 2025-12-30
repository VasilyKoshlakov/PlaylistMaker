package com.practicum.playlistmaker.playlists.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.playlists.domain.model.Playlist
import java.io.File

class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imagePlaylist: ImageView = itemView.findViewById(R.id.item_image_playlist)
    private val titlePlaylist: TextView = itemView.findViewById(R.id.item_title_playlist)
    private val trackCount: TextView = itemView.findViewById(R.id.item_track_count)

    fun bind(playlist: Playlist) {
        titlePlaylist.text = playlist.name

        trackCount.text = getTrackCountText(playlist.trackCount)
        trackCount.visibility = View.VISIBLE

        loadCoverImage(playlist.coverPath)
    }

    private fun getTrackCountText(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "$count трек"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "$count трека"
            else -> "$count треков"
        }
    }

    private fun loadCoverImage(coverPath: String?) {
        val radius = 8.dpToPx(itemView.context)

        if (coverPath != null && File(coverPath).exists()) {
            Glide.with(itemView)
                .load(File(coverPath))
                .transform(RoundedCorners(radius))
                .placeholder(R.drawable.placeholder_2)
                .error(R.drawable.placeholder_2)
                .into(imagePlaylist)
        } else {
            imagePlaylist.setImageResource(R.drawable.placeholder_2)
        }
    }

    companion object {
        fun create(parent: ViewGroup): PlaylistViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_playlist, parent, false)
            return PlaylistViewHolder(view)
        }
    }
}

private fun Int.dpToPx(context: android.content.Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}