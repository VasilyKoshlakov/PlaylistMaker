package com.practicum.playlistmaker.media.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.practicum.playlistmaker.favorites.domain.FavoritesInteractor
import com.practicum.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FavoritesViewModel(
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel(), KoinComponent {

    private val _favoritesState = MutableLiveData<FavoritesState>()
    val favoritesState: LiveData<FavoritesState> = _favoritesState

    private val gson: Gson by inject()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            favoritesInteractor.getAllFavorites().collectLatest { tracks ->
                if (tracks.isEmpty()) {
                    _favoritesState.postValue(FavoritesState.Empty)
                } else {
                    _favoritesState.postValue(FavoritesState.Content(tracks))
                }
            }
        }
    }

    fun trackToJson(track: Track): String {
        return gson.toJson(track)
    }
}

sealed interface FavoritesState {
    object Empty : FavoritesState
    data class Content(val tracks: List<Track>) : FavoritesState
}