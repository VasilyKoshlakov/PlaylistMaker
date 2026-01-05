package com.practicum.playlistmaker.media.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistsBinding
import com.practicum.playlistmaker.playlists.domain.model.Playlist
import com.practicum.playlistmaker.playlists.ui.PlaylistAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private val viewModel: PlaylistsViewModel by viewModel()

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsRecyclerView.layoutManager = layoutManager

        adapter = PlaylistAdapter()
        binding.playlistsRecyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.newPlaylist.setOnClickListener {
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
        binding.playlistsRecyclerView.visibility = View.GONE
        binding.placeholderPlaylistEmpty.visibility = View.VISIBLE
        binding.notCreatedPlaylist.visibility = View.VISIBLE
    }

    private fun showPlaylists(playlists: List<Playlist>) {
        binding.playlistsRecyclerView.visibility = View.VISIBLE
        binding.placeholderPlaylistEmpty.visibility = View.GONE
        binding.notCreatedPlaylist.visibility = View.GONE

        adapter.updatePlaylists(playlists)
    }

    private fun navigateToCreatePlaylist() {
        findNavController().navigate(R.id.action_mediaFragment_to_createPlaylistFragment)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylists()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}