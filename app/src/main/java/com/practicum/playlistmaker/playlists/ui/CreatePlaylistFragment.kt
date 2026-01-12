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
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
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

class CreatePlaylistFragment : Fragment() {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePlaylistViewModel by viewModel()

    private var selectedImageUri: Uri? = null
    private var imageSelected = false

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
            handleBackPress()
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

        setupViews()
        setupObservers()
        setupTextListeners()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        savedInstanceState?.let { restoreState(it) }
    }

    private fun setupViews() {
        binding.backButtonMedia.setOnClickListener {
            handleBackPress()
        }

        binding.image.setOnClickListener {
            pickImageFromGallery()
        }

        binding.placeholder.setOnClickListener {
            pickImageFromGallery()
        }

        binding.create.setOnClickListener {
            createPlaylist()
        }

        updateCreateButtonState(isEnabled = false)
    }

    private fun setupObservers() {
        viewModel.isCreateButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            updateCreateButtonState(isEnabled)
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

    private fun restoreState(savedInstanceState: Bundle) {
        val uriString = savedInstanceState.getString(KEY_SELECTED_IMAGE_URI)
        uriString?.let { uriStr ->
            selectedImageUri = uriStr.toUri()
            selectedImageUri?.let { uri ->
                imageSelected = true
                loadImage(uri)
            }
        }

        val title = savedInstanceState.getString(KEY_PLAYLIST_TITLE)
        val description = savedInstanceState.getString(KEY_PLAYLIST_DESCRIPTION)

        val titleEditText: EditText = binding.titlePlaylist
        val descriptionEditText: EditText = binding.descriptionPlaylist

        title?.let {
            titleEditText.setText(it)
            viewModel.updateTitle(it)

            if (it.isEmpty()) {
                binding.titlePlaylistHolder.visibility = View.GONE
            } else {
                binding.titlePlaylistHolder.visibility = View.VISIBLE
            }
        }

        description?.let {
            descriptionEditText.setText(it)
            viewModel.updateDescription(it)

            if (it.isEmpty()) {
                binding.descriptionPlaylistHolder.visibility = View.GONE
            } else {
                binding.descriptionPlaylistHolder.visibility = View.VISIBLE
            }
        }
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

    private fun createPlaylist() {
        val titleEditText: EditText = binding.titlePlaylist
        val descriptionEditText: EditText = binding.descriptionPlaylist

        val title = titleEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), R.string.playlist_name_required, Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            showLoading(true)

            try {
                val isUnique = viewModel.isPlaylistNameUnique(title)

                if (!isUnique) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.playlist_name_exists),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                val coverPath = selectedImageUri?.let { uri ->
                    saveImageToInternalStorage(uri)
                }

                val playlistId = viewModel.createPlaylist(
                    name = title,
                    description = description.ifEmpty { null },
                    coverPath = coverPath
                )

                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (playlistId > 0) {
                        showSuccessMessageAndNavigate(title)
                    } else {
                        showErrorMessage(getString(R.string.error_creating_playlist))
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showErrorMessage(e.message ?: getString(R.string.error_creating_playlist))
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
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun handleBackPress() {
        val hasChanges = viewModel.hasChanges() || imageSelected

        if (hasChanges) {
            showExitConfirmationDialog()
        } else {
            navigateBack()
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.exit_creation_title)
            .setMessage(R.string.exit_creation_message)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.exit) { dialog, _ ->
                dialog.dismiss()
                navigateBack()
            }
            .create()
            .show()
    }

    private fun showSuccessMessageAndNavigate(playlistName: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.playlist_created, playlistName),
            Toast.LENGTH_LONG
        ).show()

        navigateBack()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun navigateBack() {
        val previousDestinationId = findNavController().previousBackStackEntry?.destination?.id

        when (previousDestinationId) {
            R.id.playerFragment -> {
                findNavController().popBackStack(R.id.playerFragment, false)
            }
            R.id.mediaFragment -> {
                findNavController().popBackStack(R.id.mediaFragment, false)
            }
            else -> {
                findNavController().popBackStack()
            }
        }
    }

    private fun updateCreateButtonState(isEnabled: Boolean) {
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
        binding.create.text = if (show) "" else getString(R.string.create)

        if (!show) {
            val isEnabled = viewModel.isCreateButtonEnabled.value ?: false
            updateCreateButtonState(isEnabled)
        } else {
            binding.create.setBackgroundResource(R.drawable.button_create_playlist_disabled)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        selectedImageUri?.let { uri ->
            outState.putString(KEY_SELECTED_IMAGE_URI, uri.toString())
        }

        val titleEditText: EditText = binding.titlePlaylist
        val descriptionEditText: EditText = binding.descriptionPlaylist

        outState.putString(KEY_PLAYLIST_TITLE, titleEditText.text.toString())
        outState.putString(KEY_PLAYLIST_DESCRIPTION, descriptionEditText.text.toString())
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

    companion object {
        private const val KEY_SELECTED_IMAGE_URI = "selected_image_uri"
        private const val KEY_PLAYLIST_TITLE = "playlist_title"
        private const val KEY_PLAYLIST_DESCRIPTION = "playlist_description"
    }
}