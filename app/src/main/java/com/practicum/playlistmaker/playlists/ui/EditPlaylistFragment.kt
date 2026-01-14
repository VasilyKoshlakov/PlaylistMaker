package com.practicum.playlistmaker.playlists.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentCreatePlaylistBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditPlaylistFragment : Fragment() {

    companion object {
        const val TAG = "EditPlaylistFragment"
        const val ARG_PLAYLIST_ID = "playlist_id"
    }

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditPlaylistViewModel by viewModel()

    private var selectedImageUri: Uri? = null
    private var imageSelected = false
    private var playlistId: Long = -1L

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                imageSelected = true
                loadImage(uri)
            }
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navigateBack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistId = arguments?.getLong(ARG_PLAYLIST_ID) ?: -1L

        if (playlistId == -1L) {
            Toast.makeText(requireContext(), "Ошибка: ID плейлиста не найден", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        binding.titlePlaylistHolder.text = getString(R.string.edit_playlist)
        binding.create.text = getString(R.string.save)

        setupViews()
        setupObservers()
        setupTextListeners()
        loadPlaylistData()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    private fun loadPlaylistData() {
        lifecycleScope.launch {
            try {
                val playlist = viewModel.getPlaylistById(playlistId)

                if (playlist != null) {
                    viewModel.initialize(playlist)

                    binding.titlePlaylist.setText(playlist.name)
                    binding.descriptionPlaylist.setText(playlist.description ?: "")

                    binding.titlePlaylistHolder.visibility = View.VISIBLE
                    if (playlist.description?.isNotEmpty() == true) {
                        binding.descriptionPlaylistHolder.visibility = View.VISIBLE
                    }

                    playlist.coverPath?.let { coverPath ->
                        val file = File(coverPath)
                        if (file.exists()) {
                            loadImage(Uri.fromFile(file))
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Плейлист не найден", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки плейлиста", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun setupViews() {
        binding.backButtonMedia.setOnClickListener {
            navigateBack()
        }

        binding.image.setOnClickListener {
            pickImageFromGallery()
        }

        binding.placeholder.setOnClickListener {
            pickImageFromGallery()
        }

        binding.create.setOnClickListener {
            savePlaylist()
        }

        updateSaveButtonState(isEnabled = false)
    }

    private fun setupObservers() {
        viewModel.isSaveButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            updateSaveButtonState(isEnabled)
        }
    }

    private fun setupTextListeners() {
        val titleEditText: EditText = binding.titlePlaylist
        val descriptionEditText: EditText = binding.descriptionPlaylist

        titleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateTitle(s?.toString() ?: "")
                if (s.isNullOrEmpty()) {
                    binding.titlePlaylistHolder.visibility = View.GONE
                } else {
                    binding.titlePlaylistHolder.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateDescription(s?.toString() ?: "")
                if (s.isNullOrEmpty()) {
                    binding.descriptionPlaylistHolder.visibility = View.GONE
                } else {
                    binding.descriptionPlaylistHolder.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        titleEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                descriptionEditText.requestFocus()
                true
            } else {
                false
            }
        }

        descriptionEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val titleEditText: EditText = binding.titlePlaylist
        imm.hideSoftInputFromWindow(titleEditText.windowToken, 0)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        val chooserIntent = Intent.createChooser(intent, getString(R.string.select_image))
        pickImageLauncher.launch(chooserIntent)
    }

    private fun loadImage(uri: Uri) {
        val radius = resources.getDimensionPixelSize(R.dimen.artwork_corner_radius)

        Glide.with(this)
            .load(uri)
            .transform(RoundedCorners(radius))
            .placeholder(R.drawable.ic_placeholder_3)
            .error(R.drawable.ic_placeholder_3)
            .into(binding.image)

        binding.placeholder.visibility = View.GONE
    }

    private fun savePlaylist() {
        val title = binding.titlePlaylist.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), R.string.playlist_name_required, Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            showLoading(true)

            try {
                val coverPath = selectedImageUri?.let { uri ->
                    saveImageToInternalStorage(uri)
                } ?: viewModel.getCurrentCoverPath()

                val success = viewModel.savePlaylist(coverPath)

                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (success) {
                        showSuccessMessageAndNavigate(title)
                    } else {
                        showErrorMessage(getString(R.string.error_saving_playlist))
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showErrorMessage(e.message ?: getString(R.string.error_saving_playlist))
                }
            }
        }
    }

    private suspend fun saveImageToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val context = requireContext()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "PLAYLIST_COVER_${timeStamp}.jpg"
            val storageDir = File(context.filesDir, "playlist_covers")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            val imageFile = File(storageDir, imageFileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(imageFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            imageFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    private fun showSuccessMessageAndNavigate(playlistName: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.playlist_saved, playlistName),
            Toast.LENGTH_LONG
        ).show()

        findNavController().navigateUp()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateSaveButtonState(isEnabled: Boolean) {
        binding.create.isEnabled = isEnabled
        binding.create.alpha = if (isEnabled) 1.0f else 0.5f

        if (isEnabled) {
            binding.create.setBackgroundResource(R.drawable.button_create_playlist_enabled)
        } else {
            binding.create.setBackgroundResource(R.drawable.button_create_playlist_disabled)
        }
    }

    private fun showLoading(show: Boolean) {
        binding.create.isEnabled = !show
        binding.create.text = if (show) "" else getString(R.string.save)

        if (!show) {
            val isEnabled = viewModel.isSaveButtonEnabled.value ?: false
            updateSaveButtonState(isEnabled)
        } else {
            binding.create.setBackgroundResource(R.drawable.button_create_playlist_disabled)
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
        val rootActivity = activity as? com.practicum.playlistmaker.root.ui.RootActivity
        rootActivity?.binding?.bottomNavigationView?.visibility = View.GONE
    }

    private fun showBottomNavigation() {
        val rootActivity = activity as? com.practicum.playlistmaker.root.ui.RootActivity
        rootActivity?.binding?.bottomNavigationView?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}