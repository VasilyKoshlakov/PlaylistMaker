package com.practicum.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

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
    private lateinit var searchHistory: SearchHistory

    private var searchQuery: String = ""
    private var lastSearchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupSearchHistory()
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
    }

    private fun setupSearchHistory() {
        searchHistory = SearchHistory(getSharedPreferences("app_prefs", MODE_PRIVATE))
        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            updateHistoryVisibility()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TrackAdapter(emptyList()) { track ->
            searchHistory.addTrack(track)
            updateHistoryVisibility()
            // TODO: Add navigation to player in next sprint
        }
        recyclerView.adapter = adapter
    }

    private fun setupHistoryRecyclerView() {
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(emptyList()) { track ->
            searchHistory.addTrack(track)
            updateHistoryVisibility()
            // TODO: Add navigation to player in next sprint
        }
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupPlaceholder() {
        refreshButton.setOnClickListener {
            performSearch(lastSearchQuery)
        }
    }

    private fun showPlaceholder(isError: Boolean = false) {
        recyclerView.visibility = View.GONE
        placeholder.visibility = View.VISIBLE
        historyContainer.visibility = View.GONE

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
    }

    private fun showHistory() {
        placeholder.visibility = View.GONE
        recyclerView.visibility = View.GONE
        historyContainer.visibility = View.VISIBLE
        historyAdapter.updateTracks(searchHistory.getHistory())
    }

    private fun updateHistoryVisibility() {
        val history = searchHistory.getHistory()
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
        hidePlaceholder()

        RetrofitClient.itunesApiService.search(query).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.results.isEmpty()) {
                            showPlaceholder()
                        } else {
                            adapter.updateTracks(apiResponse.results)
                            hidePlaceholder()
                        }
                    } ?: showPlaceholder(true)
                } else {
                    showPlaceholder(true)
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                showPlaceholder(true)
            }
        })
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
        }
    }

    private fun setupSearchEditText() {
        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    searchQuery = it.toString()
                    clearButton.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                    updateHistoryVisibility()
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
                performSearch(searchQuery)
                hideKeyboard()
                true
            } else {
                false
            }
        }

        inputEditText.requestFocus()
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

    companion object {
        private const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
    }
}
