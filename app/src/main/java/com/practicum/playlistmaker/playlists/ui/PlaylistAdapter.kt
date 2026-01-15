package com.practicum.playlistmaker.playlists.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PlaylistAdapter(
    private var playlists: List<com.practicum.playlistmaker.playlists.domain.model.Playlist> = emptyList(),
    private val onPlaylistClick: (com.practicum.playlistmaker.playlists.domain.model.Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
        holder.itemView.setOnClickListener {
            onPlaylistClick(playlists[position])
        }
    }

    override fun getItemCount(): Int = playlists.size

    @SuppressLint("NotifyDataSetChanged")
    fun updatePlaylists(newPlaylists: List<com.practicum.playlistmaker.playlists.domain.model.Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }
}