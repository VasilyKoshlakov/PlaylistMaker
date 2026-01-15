package com.practicum.playlistmaker.playlists.ui

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistInfoBinding
import com.practicum.playlistmaker.player.ui.TrackAdapter
import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.util.Locale

class PlaylistInfoFragment : Fragment() {

    private var _binding: FragmentPlaylistInfoBinding? = null
    private val binding get() = _binding!!

    private val playlistId: Long by lazy {
        arguments?.getLong(PLAYLIST_ID_KEY) ?: -1L
    }

    private val viewModel: PlaylistInfoViewModel by viewModel()
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<View>

    private var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback? = null
    private var menuBottomSheetCallback: BottomSheetBehavior.BottomSheetCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupBottomSheet()
        setupMenuBottomSheet()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        if (playlistId != -1L) {
            viewModel.loadPlaylistInfo(playlistId)
        } else {
            Toast.makeText(requireContext(), "Ошибка: ID плейлиста не найден", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        hideBottomNavigation()
    }

    override fun onPause() {
        super.onPause()
        showBottomNavigation()
    }

    private fun hideBottomNavigation() {
        (activity as? com.practicum.playlistmaker.root.ui.RootActivity)?.binding?.bottomNavigationView?.isVisible = false
    }

    private fun showBottomNavigation() {
        (activity as? com.practicum.playlistmaker.root.ui.RootActivity)?.binding?.bottomNavigationView?.isVisible = true
    }

    private fun setupBackButton() {
        binding.backButtonPlaylist.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.playlistInfoTracksBottomSheet)
        bottomSheetBehavior.isHideable = false

        val displayMetrics = resources.displayMetrics
        val peekHeightPx = (200 * displayMetrics.density).toInt()
        bottomSheetBehavior.peekHeight = peekHeightPx

        bottomSheetBehavior.isDraggable = true

        bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (_binding == null) return
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (_binding == null) return
                binding.overlayPlaylistInfo.alpha = slideOffset.coerceIn(0f, 0.5f)
            }
        }

        bottomSheetCallback?.let {
            bottomSheetBehavior.addBottomSheetCallback(it)
        }
    }

    private fun setupMenuBottomSheet() {
        menuBottomSheetBehavior = BottomSheetBehavior.from(binding.playlistInfoMenuBottomSheet)
        menuBottomSheetBehavior.isHideable = true
        menuBottomSheetBehavior.peekHeight = 0
        menuBottomSheetBehavior.isDraggable = true
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        menuBottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (_binding == null) return

                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.overlayPlaylistInfo.isVisible = true
                    }
                    else -> {
                        binding.overlayPlaylistInfo.isVisible = false
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (_binding == null) return

                if (slideOffset > 0) {
                    binding.overlayPlaylistInfo.isVisible = true
                    binding.overlayPlaylistInfo.alpha = slideOffset.coerceIn(0f, 0.5f)
                } else {
                    binding.overlayPlaylistInfo.isVisible = false
                }
            }
        }

        menuBottomSheetCallback?.let {
            menuBottomSheetBehavior.addBottomSheetCallback(it)
        }

        binding.overlayPlaylistInfo.setOnClickListener {
            hideMenuBottomSheet()
        }
    }

    private fun setupRecyclerView() {
        binding.playlistInfoTracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        trackAdapter = TrackAdapter(emptyList()) { track ->
            navigateToPlayer(track)
        }
        trackAdapter.onLongClick = { track ->
            showDeleteTrackDialog(track)
        }
        binding.playlistInfoTracksRecyclerView.adapter = trackAdapter
    }

    private fun setupClickListeners() {
        binding.sharePlaylistInfo.setOnClickListener {
            sharePlaylist()
        }

        binding.menuPlaylistInfo.setOnClickListener {
            showMenuBottomSheet()
        }

        binding.playlistInfoMenuShare.setOnClickListener {
            hideMenuBottomSheet()
            sharePlaylist()
        }

        binding.playlistInfoMenuChangeInformation.setOnClickListener {
            hideMenuBottomSheet()
            navigateToEditPlaylist()
        }

        binding.playlistInfoMenuDeletePlaylist.setOnClickListener {
            hideMenuBottomSheet()
            showDeletePlaylistDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.playlist.observe(viewLifecycleOwner) { playlist ->
            playlist?.let {
                updatePlaylistInfo(it)
                updateMenuPlaylistInfo(it)
            } ?: run {
                Toast.makeText(requireContext(), "Плейлист не найден", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        viewModel.totalDuration.observe(viewLifecycleOwner) { duration ->
            binding.sumTimePlaylistInfo.text = duration
        }

        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            trackAdapter.updateTracks(tracks)

            if (tracks.isNotEmpty()) {
                binding.playlistInfoTracksBottomSheet.isVisible = true
                binding.playlistInfoTracksBottomSheetContent.isVisible = true
                binding.trackCountPlaylistInfo.text = resources.getQuantityString(
                    R.plurals.track_count,
                    tracks.size,
                    tracks.size
                )
            } else {
                binding.playlistInfoTracksBottomSheet.isVisible = false
                binding.playlistInfoTracksBottomSheetContent.isVisible = false
                binding.trackCountPlaylistInfo.text = resources.getQuantityString(
                    R.plurals.track_count,
                    0,
                    0
                )
            }
        }
    }

    private fun updatePlaylistInfo(playlist: com.practicum.playlistmaker.playlists.domain.model.Playlist) {
        binding.titlePlaylistInfo.text = playlist.name

        if (!playlist.description.isNullOrEmpty()) {
            binding.descriptionPlaylistInfo.text = playlist.description
            binding.descriptionPlaylistInfo.isVisible = true
        } else {
            binding.descriptionPlaylistInfo.isVisible = false
        }

        loadPlaylistCover(playlist.coverPath, binding.imagePlaylistInfo)
    }

    private fun updateMenuPlaylistInfo(playlist: com.practicum.playlistmaker.playlists.domain.model.Playlist) {
        val container = binding.playlistInfoMenuContainerForItemPlaylist

        container.removeAllViews()

        val playlistView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_playlist_bottom_sheet, container, false)

        val playlistImage = playlistView.findViewById<ImageView>(R.id.playlist_image)
        val playlistName = playlistView.findViewById<TextView>(R.id.playlist_name)
        val trackCount = playlistView.findViewById<TextView>(R.id.track_count)

        playlistName.text = playlist.name
        trackCount.text = resources.getQuantityString(
            R.plurals.track_count,
            playlist.trackCount,
            playlist.trackCount
        )

        loadPlaylistCover(playlist.coverPath, playlistImage)
        container.addView(playlistView)
    }

    private fun loadPlaylistCover(coverPath: String?, imageView: ImageView) {
        val radius = resources.getDimensionPixelSize(R.dimen.artwork_corner_radius)

        if (coverPath != null && File(coverPath).exists()) {
            Glide.with(this)
                .load(File(coverPath))
                .transform(CenterCrop(), RoundedCorners(radius))
                .placeholder(R.drawable.placeholder_2)
                .error(R.drawable.placeholder_2)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.placeholder_2)
        }
    }

    private fun navigateToPlayer(track: Track) {
        try {
            val trackJson = com.google.gson.Gson().toJson(track)
            val bundle = Bundle().apply {
                putString("track", trackJson)
            }
            findNavController().navigate(
                R.id.action_playlistInfoFragment_to_playerFragment,
                bundle
            )
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "Ошибка перехода к плееру", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToEditPlaylist() {
        val bundle = Bundle().apply {
            putLong("playlist_id", playlistId)
        }

        try {
            findNavController().navigate(
                R.id.action_playlistInfoFragment_to_editPlaylistFragment,
                bundle
            )
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "Ошибка перехода к редактированию", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteTrackDialog(track: Track) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление трека")
            .setMessage("Хотите удалить трек?")
            .setNegativeButton("НЕТ") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("ДА") { dialog, _ ->
                dialog.dismiss()
                deleteTrackFromPlaylist(track)
            }
            .create()
            .show()
    }

    private fun deleteTrackFromPlaylist(track: Track) {
        viewModel.deleteTrackFromPlaylist(playlistId, track.trackId)
    }

    private fun sharePlaylist() {
        runBlocking {
            try {
                val tracks = viewModel.tracks.value ?: emptyList()

                if (tracks.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "В этом плейлисте нет списка треков, которым можно поделиться",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@runBlocking
                }

                val playlist = viewModel.playlist.value
                val shareText = buildString {
                    playlist?.let {
                        append("${it.name}\n")

                        it.description?.let { description ->
                            if (description.isNotEmpty()) {
                                append("$description\n")
                            }
                        }

                        val trackCount = tracks.size
                        val trackCountText = resources.getQuantityString(
                            R.plurals.track_count,
                            trackCount,
                            trackCount
                        )
                        append("$trackCountText\n\n")

                        tracks.forEachIndexed { index, track ->
                            val trackNumber = index + 1
                            val trackDuration = if (track.trackTimeMillis != null) {
                                val minutes = track.trackTimeMillis / 60000
                                val seconds = (track.trackTimeMillis % 60000) / 1000
                                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                            } else {
                                "00:00"
                            }

                            append("$trackNumber. ${track.artistName} - ${track.trackName} ($trackDuration)")

                            if (index < tracks.size - 1) {
                                append("\n")
                            }
                        }
                    }
                }

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }

                startActivity(
                    Intent.createChooser(
                        shareIntent,
                        getString(R.string.share_playlist)
                    )
                )

            } catch (_: ActivityNotFoundException) {
                Toast.makeText(
                    requireContext(),
                    "Нет приложений для отправки сообщений",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (_: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Ошибка при отправке",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showMenuBottomSheet() {
        binding.playlistInfoMenuBottomSheet.isVisible = true
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideMenuBottomSheet() {
        if (menuBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun showDeletePlaylistDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить плейлист")
            .setMessage("Вы уверены, что хотите удалить этот плейлист?")
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Удалить") { dialog, _ ->
                dialog.dismiss()
                deletePlaylist()
            }
            .create()
            .show()
    }

    private fun deletePlaylist() {
        viewModel.deletePlaylist(playlistId)
        findNavController().popBackStack(R.id.mediaFragment, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        bottomSheetCallback?.let {
            bottomSheetBehavior.removeBottomSheetCallback(it)
        }

        menuBottomSheetCallback?.let {
            menuBottomSheetBehavior.removeBottomSheetCallback(it)
        }

        bottomSheetCallback = null
        menuBottomSheetCallback = null
        _binding = null
    }

    companion object {
        const val PLAYLIST_ID_KEY = "playlist_id"
    }
}