package com.practicum.playlistmaker

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class PlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val track = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(TRACK_KEY, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(TRACK_KEY)
        }

        track?.let { setupPlayer(it) }

        findViewById<ImageButton>(R.id.back_button_search).setOnClickListener {
            finish()
        }
    }

    private fun setupPlayer(track: Track) {
        val artworkImageView = findViewById<ImageView>(R.id.image)
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

        findViewById<TextView>(R.id.song).text = track.trackName
        findViewById<TextView>(R.id.music_group).text = track.artistName
        findViewById<TextView>(R.id.timeValue).text = track.getFormattedTrackTime()
        findViewById<TextView>(R.id.remainingTime).text = "0:00"

        track.collectionName?.let {
            findViewById<TextView>(R.id.collectionNameValue).apply {
                text = it
                visibility = TextView.VISIBLE
            }
            findViewById<TextView>(R.id.collectionName).visibility = TextView.VISIBLE
        } ?: run {
            findViewById<TextView>(R.id.collectionNameValue).visibility = TextView.GONE
            findViewById<TextView>(R.id.collectionName).visibility = TextView.GONE
        }

        track.getReleaseYear()?.let {
            findViewById<TextView>(R.id.releaseDateValue).apply {
                text = it
                visibility = TextView.VISIBLE
            }
            findViewById<TextView>(R.id.releaseDate).visibility = TextView.VISIBLE
        } ?: run {
            findViewById<TextView>(R.id.releaseDateValue).visibility = TextView.GONE
            findViewById<TextView>(R.id.releaseDate).visibility = TextView.GONE
        }

        findViewById<TextView>(R.id.genreValue).text =
            track.primaryGenreName ?: getString(R.string.unknown_genre)
        findViewById<TextView>(R.id.countryValue).text =
            track.country ?: getString(R.string.unknown_country)

        findViewById<ImageButton>(R.id.playButton).setOnClickListener {
        }

        findViewById<ImageButton>(R.id.addButton).setOnClickListener {
        }

        findViewById<ImageButton>(R.id.likeButton).setOnClickListener {
        }
    }

    companion object {
        const val TRACK_KEY = "track"
    }
}