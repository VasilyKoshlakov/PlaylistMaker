package com.practicum.playlistmaker.player.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.domain.Track
import org.koin.android.ext.android.inject

class PlayerFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        observeViewModel()

        val trackJson = arguments?.getString(TRACK_KEY)
        val track = if (trackJson != null) {
            viewModel.jsonToTrack(trackJson)
        } else {
            null
        }

        track?.let {
            setupTrackInfo(it)
            viewModel.preparePlayer(it)
        }

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        playButton.setOnClickListener {
            viewModel.togglePlayback()
        }

        likeButton.setOnClickListener {
            viewModel.toggleFavorite()
        }
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

        currentTimeTextView.text = Track.formatTime(0)

        playButton.isEnabled = false
        playButton.alpha = 0.5f
    }

    private fun observeViewModel() {
        viewModel.playerState.observe(viewLifecycleOwner) { state ->
            updatePlayerUI(state)
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            updateFavoriteButton(isFavorite)
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

    override fun onPause() {
        super.onPause()
        if (viewModel.playerState.value is PlayerState.Playing) {
            viewModel.togglePlayback()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.releasePlayer()
    }

    companion object {
        const val TRACK_KEY = "track"
    }
}