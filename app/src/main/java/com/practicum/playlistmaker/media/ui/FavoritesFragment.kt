package com.practicum.playlistmaker.media.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.player.ui.PlayerFragment
import com.practicum.playlistmaker.player.ui.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private val viewModel: FavoritesViewModel by viewModel()

    private lateinit var placeholderContainer: View
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        observeViewModel()
    }

    private fun initViews(view: View) {
        placeholderContainer = view.findViewById(R.id.placeholderContainer)
        favoritesRecyclerView = view.findViewById(R.id.favoritesRecyclerView)
    }

    private fun setupRecyclerView() {
        favoritesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TrackAdapter(emptyList()) { track ->
            navigateToPlayer(track)
        }
        favoritesRecyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.favoritesState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
        }
    }

    private fun updateUI(state: FavoritesState) {
        when (state) {
            is FavoritesState.Empty -> {
                placeholderContainer.visibility = View.VISIBLE
                favoritesRecyclerView.visibility = View.GONE
            }
            is FavoritesState.Content -> {
                placeholderContainer.visibility = View.GONE
                favoritesRecyclerView.visibility = View.VISIBLE
                adapter.updateTracks(state.tracks)
            }
        }
    }

    private fun navigateToPlayer(track: com.practicum.playlistmaker.search.domain.Track) {
        val trackJson = viewModel.trackToJson(track)

        val bundle = Bundle().apply {
            putString(PlayerFragment.TRACK_KEY, trackJson)
        }

        val currentDestination = findNavController().currentDestination
        if (currentDestination != null) {
            findNavController().navigate(
                R.id.playerFragment,
                bundle
            )
        }
    }


}