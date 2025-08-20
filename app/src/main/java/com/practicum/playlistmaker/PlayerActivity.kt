package com.practicum.playlistmaker

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
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
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        const val TRACK_KEY = "track"
    }

    private var playerState = STATE_DEFAULT
    private lateinit var mediaPlayer: MediaPlayer
    private var playbackPosition = 0

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
    private lateinit var pauseButton: ImageButton
    private lateinit var addButton: ImageButton
    private lateinit var likeButton: ImageButton

    private var currentTrack: Track? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        initViews()
        initMediaPlayer()

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
        track?.let { setupPlayer(it) }

        backButton.setOnClickListener {
            releasePlayer()
            finish()
        }

        playButton.setOnClickListener {
            playbackControl()
        }

        pauseButton.setOnClickListener {
            playbackControl()
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
        pauseButton = findViewById(R.id.pauseButton)
        addButton = findViewById(R.id.addButton)
        likeButton = findViewById(R.id.likeButton)
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener {
            playButton.isEnabled = true
            pauseButton.isEnabled = true
            playerState = STATE_PREPARED
            updateButtonVisibility()
        }
        mediaPlayer.setOnCompletionListener {
            playbackPosition = 0
            playerState = STATE_PREPARED
            updateButtonVisibility()
            currentTimeTextView.text = formatTime(0)
        }
    }

    private fun setupPlayer(track: Track) {
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
        currentTimeTextView.text = formatTime(0)

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

        addButton.setOnClickListener {
        }

        likeButton.setOnClickListener {
        }
    }

    private fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrEmpty()) {
            playButton.isEnabled = false
            pauseButton.isEnabled = false
            return
        }

        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(previewUrl)
            mediaPlayer.prepareAsync()
            playerState = STATE_DEFAULT
            updateButtonVisibility()
        } catch (e: Exception) {
            e.printStackTrace()
            playButton.isEnabled = false
            pauseButton.isEnabled = false
        }
    }

    private fun playbackControl() {
        when(playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }
            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
        }
    }

    private fun startPlayer() {
        mediaPlayer.seekTo(playbackPosition)
        mediaPlayer.start()
        playerState = STATE_PLAYING
        updateButtonVisibility()
        startProgressUpdate()
    }

    private fun pausePlayer() {
        playbackPosition = mediaPlayer.currentPosition
        mediaPlayer.pause()
        playerState = STATE_PAUSED
        updateButtonVisibility()
    }

    private fun releasePlayer() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        playbackPosition = 0
        playerState = STATE_DEFAULT
        updateButtonVisibility()
    }

    private fun updateButtonVisibility() {
        when (playerState) {
            STATE_PLAYING -> {
                playButton.visibility = View.INVISIBLE
                pauseButton.visibility = View.VISIBLE
            }
            STATE_PREPARED, STATE_PAUSED, STATE_DEFAULT -> {
                playButton.visibility = View.VISIBLE
                pauseButton.visibility = View.GONE
            }
        }
    }

    private fun startProgressUpdate() {
        Thread {
            while (playerState == STATE_PLAYING && mediaPlayer.isPlaying) {
                runOnUiThread {
                    currentTimeTextView.text = formatTime(mediaPlayer.currentPosition.toLong())
                }
                Thread.sleep(1000)
            }
        }.start()
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onPause() {
        super.onPause()
        if (playerState == STATE_PLAYING) {
            pausePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}