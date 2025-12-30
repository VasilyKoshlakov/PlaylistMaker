package com.practicum.playlistmaker.player.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.playlists.domain.model.Playlist
import java.io.File

class PlaylistBottomSheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val playlistImage: ImageView = itemView.findViewById(R.id.playlist_image)
    private val playlistName: TextView = itemView.findViewById(R.id.playlist_name)
    private val trackCount: TextView = itemView.findViewById(R.id.track_count)

    fun bind(
        playlist: Playlist,
        onPlaylistClick: (Playlist) -> Unit
    ) {
        playlistName.text = playlist.name

        trackCount.text = getTrackCountText(playlist.trackCount)

        loadCoverImage(playlist.coverPath)

        itemView.setOnClickListener {
            onPlaylistClick(playlist)
        }
    }

    private fun getTrackCountText(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "$count трек"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "$count трека"
            else -> "$count треков"
        }
    }

    private fun loadCoverImage(coverPath: String?) {
        val radius = 2.dpToPx(itemView.context)

        val requestOptions = RequestOptions()
            .transform(CenterCrop(), RoundedCorners(radius))
            .override(45.dpToPx(itemView.context))

        if (coverPath != null && File(coverPath).exists()) {
            Glide.with(itemView)
                .load(File(coverPath))
                .apply(requestOptions)
                .placeholder(R.drawable.placeholder_2)
                .error(R.drawable.placeholder_2)
                .into(playlistImage)
        } else {
            Glide.with(itemView)
                .load(R.drawable.placeholder_2)
                .apply(requestOptions)
                .into(playlistImage)
        }
    }

    companion object {
        fun create(parent: ViewGroup): PlaylistBottomSheetViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_playlist_bottom_sheet, parent, false)
            return PlaylistBottomSheetViewHolder(view)
        }
    }
}
private fun Int.dpToPx(context: android.content.Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}