package com.practicum.playlistmaker.search.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.creator.AppCreator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.player.domain.Track
import com.practicum.playlistmaker.player.ui.TrackAdapter
import com.practicum.playlistmaker.player.ui.PlayerActivity

class SearchActivity : AppCompatActivity() {

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(AppCreator.provideSearchInteractor())
    }

    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter
    private lateinit var placeholder: View
    private lateinit var placeholderImage: ImageView
    private lateinit var refreshButton: Button
    private lateinit var errorMessage: TextView
    private lateinit var historyTitle: TextView
    private lateinit var clearHistoryButton: Button
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var historyContainer: View
    private lateinit var progressBar: ProgressBar

    private var isTextChangedByUser = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupRecyclerView()
        setupHistoryRecyclerView()
        setupBackButton()
        setupClearButton()
        setupSearchEditText()
        setupPlaceholder()

        observeViewModel()

        viewModel.showSearchHistory()
    }

    private fun initViews() {
        inputEditText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.clearIcon)
        recyclerView = findViewById(R.id.tracks_recycler_view)
        placeholder = findViewById(R.id.placeholder)
        placeholderImage = findViewById(R.id.placeholderImage)
        refreshButton = findViewById(R.id.refreshButton)
        errorMessage = findViewById(R.id.errorMessage)
        historyTitle = findViewById(R.id.historySearch)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        historyRecyclerView = findViewById(R.id.history_recycler_view)
        historyContainer = findViewById(R.id.history_container)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TrackAdapter(emptyList()) { track ->
            viewModel.addTrackToHistory(track)
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.TRACK_KEY, track)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun setupHistoryRecyclerView() {
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(emptyList()) { track ->
            viewModel.addTrackToHistory(track)
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.TRACK_KEY, track)
            }
            startActivity(intent)
        }
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupPlaceholder() {
        refreshButton.setOnClickListener {
            val currentState = viewModel.searchState.value
            val query = currentState?.searchQuery ?: ""
            if (query.isNotEmpty()) {
                viewModel.searchTracks(query)
            }
        }

        clearHistoryButton.setOnClickListener {
            viewModel.clearSearchHistory()
        }
    }

    private fun observeViewModel() {
        viewModel.searchState.observe(this) { state ->
            updateUI(state)

            if (inputEditText.text.toString() != state.searchQuery) {
                isTextChangedByUser = false
                inputEditText.setText(state.searchQuery)
                inputEditText.setSelection(state.searchQuery.length)
                isTextChangedByUser = true
            }
        }
    }

    private fun updateUI(state: SearchState) {
        when {
            state.isLoading -> showLoading()
            state.isError -> showError()
            state.showHistory -> showHistory(state.tracks)
            state.tracks.isEmpty() -> showEmptyResults()
            else -> showResults(state.tracks)
        }
    }

    private fun showLoading() {
        recyclerView.visibility = View.GONE
        placeholder.visibility = View.GONE
        historyContainer.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun showError() {
        recyclerView.visibility = View.GONE
        placeholder.visibility = View.VISIBLE
        historyContainer.visibility = View.GONE
        progressBar.visibility = View.GONE

        placeholderImage.setImageResource(R.drawable.connection_problem_placeholder_120)
        errorMessage.text = getString(R.string.connection_error)
        refreshButton.visibility = View.VISIBLE
    }

    private fun showEmptyResults() {
        recyclerView.visibility = View.GONE
        placeholder.visibility = View.VISIBLE
        historyContainer.visibility = View.GONE
        progressBar.visibility = View.GONE

        placeholderImage.setImageResource(R.drawable.not_found_placeholder_120)
        errorMessage.text = getString(R.string.nothing_found)
        refreshButton.visibility = View.GONE
    }

    private fun showResults(tracks: List<Track>) {
        placeholder.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        historyContainer.visibility = View.GONE
        progressBar.visibility = View.GONE

        adapter.updateTracks(tracks)
    }

    private fun showHistory(tracks: List<Track>) {
        placeholder.visibility = View.GONE
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE

        historyAdapter.updateTracks(tracks)
        historyContainer.visibility = if (tracks.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.button_back_search).setOnClickListener {
            finish()
        }
    }

    private fun setupClearButton() {
        clearButton.setOnClickListener {
            inputEditText.setText("")
            hideKeyboard()
            viewModel.updateSearchQuery("")
            viewModel.showSearchHistory()
        }
    }

    private fun setupSearchEditText() {
        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isTextChangedByUser) return

                s?.let { sequence ->
                    val query = sequence.toString()
                    clearButton.visibility = if (sequence.isEmpty()) View.GONE else View.VISIBLE

                    if (query.isEmpty()) {
                        viewModel.showSearchHistory()
                    } else {
                        viewModel.searchTracks(query)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && inputEditText.text.isEmpty()) {
                viewModel.showSearchHistory()
            }
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = inputEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.searchTracks(query)
                }
                hideKeyboard()
                true
            } else {
                false
            }
        }

        inputEditText.requestFocus()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, viewModel.searchState.value?.searchQuery ?: "")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        if (savedQuery.isNotEmpty()) {
            viewModel.updateSearchQuery(savedQuery)
            inputEditText.setText(savedQuery)
        }
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
    }
}