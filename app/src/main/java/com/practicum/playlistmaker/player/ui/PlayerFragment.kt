package com.practicum.playlistmaker.player.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlayerWithBottomSheetBinding
import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class PlayerFragment : Fragment() {



    private var _binding: FragmentPlayerWithBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerViewModel by inject()

    private lateinit var backButton: ImageButton
    private lateinit var artworkImageView: ImageView
    private lateinit var songTextView: TextView
    private lateinit var artistTextView: TextView
    private lateinit var durationTextView: TextView
    private lateinit var currentTimeTextView: TextView
    private lateinit var collectionNameTextView: TextView
    private lateinit var collectionNameValueTextView: TextView
    private lateinit var releaseDateTextView: TextView
    private lateinit var releaseDateValueTextView: TextView
    private lateinit var genreValueTextView: TextView
    private lateinit var countryValueTextView: TextView
    private lateinit var playButton: ImageButton
    private lateinit var likeButton: ImageButton
    private lateinit var addButton: ImageButton

    private lateinit var bottomSheet: View
    private lateinit var overlay: View
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var bottomSheetRecyclerView: RecyclerView
    private lateinit var newPlaylistButton: Button
    private lateinit var bottomSheetAdapter: PlaylistBottomSheetAdapter

    private var currentTrack: Track? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerWithBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)

        val trackJson = arguments?.getString(TRACK_KEY)
        val track = if (trackJson != null) {
            viewModel.jsonToTrack(trackJson)
        } else {
            null
        }

        track?.let {
            setupTrackInfo(it)
            currentTrack = it

            viewModel.preparePlayer(it)
        } ?: run {
            findNavController().popBackStack()
            return
        }

        setupListeners()
        observeViewModel()
        setupBottomSheet(view)
    }

    private fun initViews(view: View) {
        backButton = view.findViewById(R.id.back_button_search)
        artworkImageView = view.findViewById(R.id.image)
        songTextView = view.findViewById(R.id.song)
        artistTextView = view.findViewById(R.id.music_group)
        durationTextView = view.findViewById(R.id.timeValue)
        currentTimeTextView = view.findViewById(R.id.remainingTime)
        collectionNameTextView = view.findViewById(R.id.collectionName)
        collectionNameValueTextView = view.findViewById(R.id.collectionNameValue)
        releaseDateTextView = view.findViewById(R.id.releaseDate)
        releaseDateValueTextView = view.findViewById(R.id.releaseDateValue)
        genreValueTextView = view.findViewById(R.id.genreValue)
        countryValueTextView = view.findViewById(R.id.countryValue)
        playButton = view.findViewById(R.id.playButton)
        likeButton = view.findViewById(R.id.likeButton)
        addButton = view.findViewById(R.id.addButton)

        currentTimeTextView.text = Track.formatTime(0)

        playButton.isEnabled = false
        playButton.alpha = 0.5f

        likeButton.isEnabled = true
        addButton.isEnabled = true
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        playButton.setOnClickListener {
            viewModel.togglePlayback()
        }

        likeButton.setOnClickListener {
            viewModel.toggleFavorite()
        }

        addButton.setOnClickListener {
            showAddToPlaylistBottomSheet()
        }
    }

    private fun setupBottomSheet(view: View) {
        bottomSheet = view.findViewById(R.id.playlists_bottom_sheet)
        overlay = view.findViewById(R.id.overlay)
        bottomSheetRecyclerView = view.findViewById(R.id.playlists_recycler_view)
        newPlaylistButton = view.findViewById(R.id.new_playlist_button)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.isDraggable = true

        setupBottomSheetRecyclerView()
        setupBottomSheetListeners()
        setupBottomSheetCallbacks()
    }

    private fun setupBottomSheetRecyclerView() {
        bottomSheetRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        bottomSheetAdapter = PlaylistBottomSheetAdapter(
            onPlaylistClick = { playlist ->
                currentTrack?.let { track ->
                    lifecycleScope.launch {
                        viewModel.addTrackToPlaylist(playlist.playlistId, track)
                    }
                } ?: run {
                    Toast.makeText(requireContext(), "Ошибка: трек не найден", Toast.LENGTH_SHORT).show()
                }
            }
        )
        bottomSheetRecyclerView.adapter = bottomSheetAdapter
    }

    private fun setupBottomSheetListeners() {
        newPlaylistButton.setOnClickListener {
            hideAddToPlaylistBottomSheet()
            handler.postDelayed({
                try {
                    findNavController().navigate(R.id.action_playerFragment_to_createPlaylistFragment)
                } catch (_: Exception) {
                    findNavController().popBackStack()
                }
            }, 300)
        }

        overlay.setOnClickListener {
            hideAddToPlaylistBottomSheet()
        }
    }

    private fun setupBottomSheetCallbacks() {
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        overlay.visibility = View.GONE
                        bottomSheet.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_EXPANDED,
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        overlay.visibility = View.VISIBLE
                        bottomSheet.visibility = View.VISIBLE
                    }
                    else -> {}
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                overlay.alpha = slideOffset.coerceIn(0f, 1f)
            }
        })
    }

    private fun observeViewModel() {
        viewModel.playerState.observe(viewLifecycleOwner) { state ->
            updatePlayerUI(state)
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            updateFavoriteButton(isFavorite)
        }

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            bottomSheetAdapter.updatePlaylists(playlists)
        }

        viewModel.addToPlaylistResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                when (it) {
                    is AddToPlaylistResult.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "Добавлено в плейлист \"${it.playlistName}\"",
                            Toast.LENGTH_SHORT
                        ).show()
                        hideAddToPlaylistBottomSheet()
                    }
                    is AddToPlaylistResult.AlreadyExists -> {
                        Toast.makeText(
                            requireContext(),
                            "Трек уже добавлен в плейлист \"${it.playlistName}\"",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is AddToPlaylistResult.Error -> {
                        Toast.makeText(
                            requireContext(),
                            it.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    AddToPlaylistResult.None -> {}
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updatePlayerUI(state: PlayerState) {
        currentTimeTextView.text = state.formattedCurrentTime

        when (state) {
            is PlayerState.Loading -> {
                playButton.setImageResource(R.drawable.ic_play_button_100)
                playButton.isEnabled = false
                playButton.alpha = 0.5f
            }
            is PlayerState.Error -> {
                playButton.setImageResource(R.drawable.ic_play_button_100)
                playButton.isEnabled = false
                playButton.alpha = 0.5f
            }
            is PlayerState.Prepared -> {
                playButton.setImageResource(R.drawable.ic_play_button_100)
                playButton.isEnabled = true
                playButton.alpha = 1.0f
            }
            is PlayerState.Playing -> {
                playButton.setImageResource(R.drawable.ic_pause_button_100)
                playButton.isEnabled = true
                playButton.alpha = 1.0f
            }
            is PlayerState.Paused -> {
                playButton.setImageResource(R.drawable.ic_play_button_100)
                playButton.isEnabled = true
                playButton.alpha = 1.0f
            }
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        if (isFavorite) {
            likeButton.setImageResource(R.drawable.ic_like_button_active_51)
        } else {
            likeButton.setImageResource(R.drawable.ic_like_button_51)
        }
    }

    private fun setupTrackInfo(track: Track) {
        val artworkUrl = track.getCoverArtwork()
        if (artworkUrl != null) {
            Glide.with(this)
                .load(artworkUrl)
                .placeholder(R.drawable.placeholder_2)
                .error(R.drawable.placeholder_2)
                .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.artwork_corner_radius)))
                .into(artworkImageView)
        } else {
            artworkImageView.setImageResource(R.drawable.placeholder_2)
        }

        songTextView.text = track.trackName
        artistTextView.text = track.artistName
        durationTextView.text = track.getFormattedTrackTime()

        setupOptionalFields(track)
    }

    private fun setupOptionalFields(track: Track) {
        track.collectionName?.let { collectionName ->
            collectionNameValueTextView.text = collectionName
            collectionNameValueTextView.visibility = View.VISIBLE
            collectionNameTextView.visibility = View.VISIBLE
        } ?: run {
            collectionNameValueTextView.visibility = View.GONE
            collectionNameTextView.visibility = View.GONE
        }

        track.getReleaseYear()?.let { releaseYear ->
            releaseDateValueTextView.text = releaseYear
            releaseDateValueTextView.visibility = View.VISIBLE
            releaseDateTextView.visibility = View.VISIBLE
        } ?: run {
            releaseDateValueTextView.visibility = View.GONE
            releaseDateTextView.visibility = View.GONE
        }

        genreValueTextView.text = track.primaryGenreName ?: getString(R.string.unknown_genre)
        countryValueTextView.text = track.country ?: getString(R.string.unknown_country)
    }

    private fun showAddToPlaylistBottomSheet() {
        currentTrack?.let { track ->
            lifecycleScope.launch {
                viewModel.loadPlaylists()
            }

            overlay.visibility = View.VISIBLE
            bottomSheet.visibility = View.VISIBLE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } ?: run {
            Toast.makeText(requireContext(), "Ошибка: трек не найден", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideAddToPlaylistBottomSheet() {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.releasePlayer()
        handler.removeCallbacksAndMessages(null)

        Glide.with(this).clear(artworkImageView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }

    companion object {
        const val TRACK_KEY = "track"
    }
}