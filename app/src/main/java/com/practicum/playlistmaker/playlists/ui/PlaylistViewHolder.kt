package com.practicum.playlistmaker.playlists.ui

import android.util.TypedValue
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

        trackCount.text = itemView.context.resources.getQuantityString(
            R.plurals.track_count,
            playlist.trackCount,
            playlist.trackCount
        )
        trackCount.visibility = View.VISIBLE

        loadCoverImage(playlist.coverPath)
    }

    private fun loadCoverImage(coverPath: String?) {
        val radius = dpToPx(8, itemView.context)

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

    private fun dpToPx(dp: Int, context: android.content.Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    companion object {
        fun create(parent: ViewGroup): PlaylistViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_playlist, parent, false)
            return PlaylistViewHolder(view)
        }
    }
}