package com.practicum.playlistmaker.presentation.search

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.AppCreator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.api.SearchInteractor
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import com.practicum.playlistmaker.presentation.player.PlayerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity() {

    private lateinit var searchInteractor: SearchInteractor
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

    private var searchQuery: String = ""
    private var lastSearchQuery: String = ""

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val debounceDelay = 2000L
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchInteractor = AppCreator.provideSearchInteractor()

        initViews()
        setupRecyclerView()
        setupHistoryRecyclerView()
        setupBackButton()
        setupClearButton()
        setupSearchEditText()
        setupPlaceholder()

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        }
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

        val debouncedTrackClick = debounceClick<Track> { track ->
            searchInteractor.addTrackToHistory(track)
            updateHistoryVisibility()

            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.TRACK_KEY, track)
            }
            startActivity(intent)
        }

        adapter = TrackAdapter(emptyList(), debouncedTrackClick)
        recyclerView.adapter = adapter
    }

    private fun setupHistoryRecyclerView() {
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        val debouncedHistoryClick = debounceClick<Track> { track ->
            searchInteractor.addTrackToHistory(track)
            updateHistoryVisibility()

            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.TRACK_KEY, track)
            }
            startActivity(intent)
        }

        historyAdapter = TrackAdapter(emptyList(), debouncedHistoryClick)
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupPlaceholder() {
        refreshButton.setOnClickListener {
            performSearch(lastSearchQuery)
        }

        clearHistoryButton.setOnClickListener {
            searchInteractor.clearSearchHistory()
            updateHistoryVisibility()
        }
    }

    private fun showPlaceholder(isError: Boolean = false) {
        recyclerView.visibility = View.GONE
        placeholder.visibility = View.VISIBLE
        historyContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
        isLoading = false

        if (isError) {
            placeholderImage.setImageResource(R.drawable.connection_problem_placeholder_120)
            errorMessage.text = getString(R.string.connection_error)
            refreshButton.visibility = View.VISIBLE
        } else {
            placeholderImage.setImageResource(R.drawable.not_found_placeholder_120)
            errorMessage.text = getString(R.string.nothing_found)
            refreshButton.visibility = View.GONE
        }
    }

    private fun hidePlaceholder() {
        placeholder.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        historyContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
        isLoading = false
    }

    private fun showHistory() {
        placeholder.visibility = View.GONE
        recyclerView.visibility = View.GONE
        historyContainer.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        isLoading = false
        historyAdapter.updateTracks(searchInteractor.getSearchHistory())
    }

    private fun updateHistoryVisibility() {
        val history = searchInteractor.getSearchHistory()
        val shouldShowHistory = inputEditText.text.isEmpty() &&
                inputEditText.hasFocus() &&
                history.isNotEmpty()

        if (shouldShowHistory) {
            showHistory()
        } else {
            historyContainer.visibility = View.GONE
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            adapter.updateTracks(emptyList())
            showPlaceholder()
            return
        }

        lastSearchQuery = query
        showLoading()

        CoroutineScope(Dispatchers.Main).launch {
            val tracks = withContext(Dispatchers.IO) {
                searchInteractor.searchTracks(query)
            }

            hideLoading()

            if (tracks.isEmpty()) {
                showPlaceholder()
            } else {
                adapter.updateTracks(tracks)
                hidePlaceholder()
            }
        }
    }

    private fun showLoading() {
        isLoading = true
        recyclerView.visibility = View.GONE
        placeholder.visibility = View.GONE
        historyContainer.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        isLoading = false
        progressBar.visibility = View.GONE
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
            searchQuery = ""
            clearButton.visibility = View.GONE
            adapter.updateTracks(emptyList())
            updateHistoryVisibility()
            searchRunnable?.let { runnable -> handler.removeCallbacks(runnable) }
        }
    }

    private fun setupSearchEditText() {
        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { sequence ->
                    searchQuery = sequence.toString()
                    clearButton.visibility = if (sequence.isEmpty()) View.GONE else View.VISIBLE
                    updateHistoryVisibility()
                    searchRunnable?.let { runnable -> handler.removeCallbacks(runnable) }
                    searchRunnable = Runnable {
                        if (searchQuery.isNotEmpty() && !isLoading) {
                            performSearch(searchQuery)
                        }
                    }
                    handler.postDelayed(searchRunnable!!, debounceDelay)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            updateHistoryVisibility()
            if (hasFocus && inputEditText.text.isNotEmpty()) {
                showKeyboard()
            }
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchRunnable?.let { runnable -> handler.removeCallbacks(runnable) }
                performSearch(searchQuery)
                hideKeyboard()
                true
            } else {
                false
            }
        }

        inputEditText.requestFocus()
    }

    private inline fun <T> debounceClick(
        crossinline onClick: (T) -> Unit
    ): (T) -> Unit {
        var lastClickTime = 0L
        val debounceClickDelay = 1000L

        return { item ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > debounceClickDelay) {
                lastClickTime = currentTime
                onClick(item)
            }
        }
    }

    private fun showKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(inputEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    private fun restoreState(savedInstanceState: Bundle) {
        searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        inputEditText.setText(searchQuery)
        if (searchQuery.isNotEmpty()) {
            performSearch(searchQuery)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreState(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
    }
}
