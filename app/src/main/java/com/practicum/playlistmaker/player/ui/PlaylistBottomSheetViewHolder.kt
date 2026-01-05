package com.practicum.playlistmaker.player.ui

import android.util.TypedValue
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

        trackCount.text = itemView.context.resources.getQuantityString(
            R.plurals.track_count,
            playlist.trackCount,
            playlist.trackCount
        )

        loadCoverImage(playlist.coverPath)

        itemView.setOnClickListener {
            onPlaylistClick(playlist)
        }
    }

    private fun loadCoverImage(coverPath: String?) {
        val radius = dpToPx(2, itemView.context)

        val requestOptions = RequestOptions()
            .transform(CenterCrop(), RoundedCorners(radius))
            .override(dpToPx(45, itemView.context))

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

    private fun dpToPx(dp: Int, context: android.content.Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    companion object {
        fun create(parent: ViewGroup): PlaylistBottomSheetViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_playlist_bottom_sheet, parent, false)
            return PlaylistBottomSheetViewHolder(view)
        }
    }
}