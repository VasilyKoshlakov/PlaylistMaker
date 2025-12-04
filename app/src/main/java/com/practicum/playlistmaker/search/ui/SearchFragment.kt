package com.practicum.playlistmaker.search.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.player.ui.PlayerFragment
import com.practicum.playlistmaker.player.ui.TrackAdapter
import com.practicum.playlistmaker.search.domain.Track
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModel()

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupHistoryRecyclerView()
        setupClearButton()
        setupSearchEditText()
        setupPlaceholder()

        observeViewModel()

        viewModel.restoreSearchState()

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.restoreSearchState()
    }

    private fun initViews(view: View) {
        inputEditText = view.findViewById(R.id.inputEditText)
        clearButton = view.findViewById(R.id.clearIcon)
        recyclerView = view.findViewById(R.id.tracks_recycler_view)
        placeholder = view.findViewById(R.id.placeholder)
        placeholderImage = view.findViewById(R.id.placeholderImage)
        refreshButton = view.findViewById(R.id.refreshButton)
        errorMessage = view.findViewById(R.id.errorMessage)
        historyTitle = view.findViewById(R.id.historySearch)
        clearHistoryButton = view.findViewById(R.id.clearHistoryButton)
        historyRecyclerView = view.findViewById(R.id.history_recycler_view)
        historyContainer = view.findViewById(R.id.history_container)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TrackAdapter(emptyList()) { track ->
            viewModel.onTrackClick(track)
            navigateToPlayer(track)
        }
        recyclerView.adapter = adapter
    }

    private fun setupHistoryRecyclerView() {
        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = TrackAdapter(emptyList()) { track ->
            viewModel.onTrackClick(track)
            navigateToPlayer(track)
        }
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupPlaceholder() {
        refreshButton.setOnClickListener {
            viewModel.refreshSearch()
        }

        clearHistoryButton.setOnClickListener {
            viewModel.clearSearchHistory()
        }
    }

    private fun observeViewModel() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            state?.let { updateUI(it) }
        }
    }

    private fun updateUI(state: SearchState) {
        when (state) {
            is SearchState.Loading -> showLoading()
            is SearchState.Error -> showError()
            is SearchState.History -> showHistory(state.tracks)
            is SearchState.Empty -> showEmptyResults()
            is SearchState.Content -> showResults(state.tracks)
        }

        updateSearchEditText(state.searchQuery)
    }

    private fun updateSearchEditText(query: String) {
        if (inputEditText.text.toString() != query && !inputEditText.hasFocus()) {
            isTextChangedByUser = false
            inputEditText.setText(query)
            inputEditText.setSelection(query.length)
            isTextChangedByUser = true
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

        clearButton.visibility = if (inputEditText.text.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun showHistory(tracks: List<Track>) {
        placeholder.visibility = View.GONE
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE

        historyAdapter.updateTracks(tracks)
        historyContainer.visibility = if (tracks.isEmpty()) View.GONE else View.VISIBLE

        clearButton.visibility = View.GONE
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

                    viewModel.updateSearchQuery(query)

                    if (query.isNotEmpty()) {
                        viewModel.searchTracks(query)
                    } else {
                        viewModel.showSearchHistory()
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
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    private fun navigateToPlayer(track: Track) {
        val trackJson = viewModel.trackToJson(track)
        val bundle = Bundle().apply {
            putString(PlayerFragment.TRACK_KEY, trackJson)
        }

        findNavController().navigate(
            R.id.action_searchFragment_to_playerFragment,
            bundle
        )
    }

    private fun restoreState(savedInstanceState: Bundle) {
        val savedQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        if (savedQuery.isNotEmpty()) {
            viewModel.updateSearchQuery(savedQuery)
            inputEditText.setText(savedQuery)
        } else {
            viewModel.showSearchHistory()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val currentQuery = when (val state = viewModel.searchState.value) {
            is SearchState.Content -> state.searchQuery
            is SearchState.Empty -> state.searchQuery
            is SearchState.Error -> state.searchQuery
            is SearchState.History -> state.searchQuery
            is SearchState.Loading -> state.searchQuery
            null -> ""
        }
        outState.putString(SEARCH_QUERY_KEY, currentQuery)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { restoreState(it) }
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
    }
}