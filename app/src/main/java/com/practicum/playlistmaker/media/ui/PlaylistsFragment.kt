package com.practicum.playlistmaker.media.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.playlists.ui.PlaylistAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private val viewModel: PlaylistsViewModel by viewModel()

    private lateinit var newPlaylistButton: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeholderImage: View
    private lateinit var placeholderText: View
    private lateinit var adapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        newPlaylistButton = view.findViewById(R.id.newPlaylist)
        recyclerView = view.findViewById(R.id.playlists_recycler_view)
        placeholderImage = view.findViewById(R.id.placeholderPlaylistEmpty)
        placeholderText = view.findViewById(R.id.not_created_playlist)
    }

    private fun setupRecyclerView() {
        val layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.layoutManager = layoutManager

        adapter = PlaylistAdapter()
        recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        newPlaylistButton.setOnClickListener {
            navigateToCreatePlaylist()
        }
    }

    private fun observeViewModel() {
        viewModel.playlistsState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
        }
    }

    private fun updateUI(state: PlaylistsState) {
        when (state) {
            is PlaylistsState.Empty -> {
                showPlaceholder()
            }
            is PlaylistsState.Content -> {
                showPlaylists(state.playlists)
            }
        }
    }

    private fun showPlaceholder() {
        recyclerView.visibility = View.GONE
        placeholderImage.visibility = View.VISIBLE
        placeholderText.visibility = View.VISIBLE
    }

    private fun showPlaylists(playlists: List<com.practicum.playlistmaker.playlists.domain.model.Playlist>) {
        recyclerView.visibility = View.VISIBLE
        placeholderImage.visibility = View.GONE
        placeholderText.visibility = View.GONE

        adapter.updatePlaylists(playlists)
    }

    private fun navigateToCreatePlaylist() {
        findNavController().navigate(R.id.action_mediaFragment_to_createPlaylistFragment)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylists()
    }
}