package com.practicum.playlistmaker.player.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.creator.AppCreator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.player.domain.Track

class PlayerActivity : AppCompatActivity() {

    private val viewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory(AppCreator.providePlayerInteractor())
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        initViews()
        observeViewModel()

        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(TRACK_KEY, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(TRACK_KEY)
        }

        track?.let {
            setupTrackInfo(it)
            viewModel.preparePlayer(it)
        }

        backButton.setOnClickListener {
            finish()
        }

        playButton.setOnClickListener {
            viewModel.togglePlayback()
        }
    }

    private fun initViews() {
        backButton = findViewById(R.id.back_button_search)
        artworkImageView = findViewById(R.id.image)
        songTextView = findViewById(R.id.song)
        artistTextView = findViewById(R.id.music_group)
        durationTextView = findViewById(R.id.timeValue)
        currentTimeTextView = findViewById(R.id.remainingTime)
        collectionNameTextView = findViewById(R.id.collectionName)
        collectionNameValueTextView = findViewById(R.id.collectionNameValue)
        releaseDateTextView = findViewById(R.id.releaseDate)
        releaseDateValueTextView = findViewById(R.id.releaseDateValue)
        genreValueTextView = findViewById(R.id.genreValue)
        countryValueTextView = findViewById(R.id.countryValue)
        playButton = findViewById(R.id.playButton)
    }

    private fun observeViewModel() {
        viewModel.playerState.observe(this) { state ->
            updatePlayerUI(state)
        }
    }

    private fun updatePlayerUI(state: PlayerState) {
        playButton.isEnabled = state.isPrepared
        currentTimeTextView.text = state.formattedCurrentTime

        val iconRes = if (state.isPlaying) {
            R.drawable.ic_pause_button_100
        } else {
            R.drawable.ic_play_button_100
        }
        playButton.setImageResource(iconRes)
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
        currentTimeTextView.text = "00:00"

        track.collectionName?.let {
            collectionNameValueTextView.text = it
            collectionNameValueTextView.visibility = View.VISIBLE
            collectionNameTextView.visibility = View.VISIBLE
        } ?: run {
            collectionNameValueTextView.visibility = View.GONE
            collectionNameTextView.visibility = View.GONE
        }

        track.getReleaseYear()?.let {
            releaseDateValueTextView.text = it
            releaseDateValueTextView.visibility = View.VISIBLE
            releaseDateTextView.visibility = View.VISIBLE
        } ?: run {
            releaseDateValueTextView.visibility = View.GONE
            releaseDateTextView.visibility = View.GONE
        }

        genreValueTextView.text = track.primaryGenreName ?: getString(R.string.unknown_genre)
        countryValueTextView.text = track.country ?: getString(R.string.unknown_country)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.releasePlayer()
    }

    companion object {
        const val TRACK_KEY = "track"
    }
}