package com.practicum.playlistmaker.presentation.player

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.AppCreator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.api.PlayerInteractor
import com.practicum.playlistmaker.domain.model.Track

class PlayerActivity : AppCompatActivity() {

    private lateinit var playerInteractor: PlayerInteractor
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
    private lateinit var addButton: ImageButton
    private lateinit var likeButton: ImageButton

    private var currentTrack: Track? = null
    private var isPlaying = false

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var progressUpdateRunnable: Runnable
    private val progressUpdateDelay = 500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        playerInteractor = AppCreator.providePlayerInteractor()

        initViews()
        initHandler()
        setupPlayer()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(TRACK_KEY, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(TRACK_KEY)
        }

        currentTrack = track
        track?.let { setupPlayerUI(it) }

        backButton.setOnClickListener {
            releasePlayer()
            finish()
        }

        playButton.setOnClickListener {
            playbackControl()
        }

        addButton.setOnClickListener {
        }

        likeButton.setOnClickListener {
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
        addButton = findViewById(R.id.addButton)
        likeButton = findViewById(R.id.likeButton)
    }

    private fun initHandler() {
        progressUpdateRunnable = object : Runnable {
            override fun run() {
                if (isPlaying) {
                    currentTimeTextView.text = playerInteractor.getFormattedTime(
                        playerInteractor.getCurrentPosition().toLong()
                    )
                    handler.postDelayed(this, progressUpdateDelay)
                }
            }
        }
    }

    private fun setupPlayer() {
        playerInteractor.setOnCompletionListener {
            runOnUiThread {
                isPlaying = false
                updatePlayButtonIcon()
                currentTimeTextView.text = playerInteractor.getFormattedTime(0)
                handler.removeCallbacks(progressUpdateRunnable)
            }
        }
    }

    private fun setupPlayerUI(track: Track) {
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
        currentTimeTextView.text = playerInteractor.getFormattedTime(0)

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

        preparePlayer(track.previewUrl)
    }

    private fun preparePlayer(previewUrl: String?) {
        playerInteractor.preparePlayer(previewUrl) { isPrepared ->
            runOnUiThread {
                playButton.isEnabled = isPrepared
            }
        }
    }

    private fun playbackControl() {
        if (isPlaying) {
            pausePlayer()
        } else {
            startPlayer()
        }
    }

    private fun startPlayer() {
        playerInteractor.startPlayer()
        isPlaying = true
        updatePlayButtonIcon()
        startProgressUpdate()
    }

    private fun pausePlayer() {
        playerInteractor.pausePlayer()
        isPlaying = false
        updatePlayButtonIcon()
        handler.removeCallbacks(progressUpdateRunnable)
    }

    private fun releasePlayer() {
        playerInteractor.releasePlayer()
        isPlaying = false
        updatePlayButtonIcon()
        handler.removeCallbacks(progressUpdateRunnable)
    }

    private fun updatePlayButtonIcon() {
        val iconRes = if (isPlaying) {
            R.drawable.ic_pause_button_100
        } else {
            R.drawable.ic_play_button_100
        }
        playButton.setImageResource(iconRes)
    }

    private fun startProgressUpdate() {
        handler.post(progressUpdateRunnable)
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            pausePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(progressUpdateRunnable)
        playerInteractor.releasePlayer()
    }

    companion object {
        const val TRACK_KEY = "track"
    }
}


