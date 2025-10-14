package com.practicum.playlistmaker.search.data

import com.practicum.playlistmaker.search.domain.Track
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) {
    private val historyKey = "search_history"

    fun addTrack(track: Track) {
        val history = getHistory().toMutableList()

        history.removeAll { it.trackId == track.trackId }

        history.add(0, track)

        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.lastIndex)
        }

        saveHistory(history)
    }

    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(historyKey, null)
        return if (json != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun clearHistory() {
        sharedPreferences.edit().remove(historyKey).apply()
    }

    private fun saveHistory(history: List<Track>) {
        if (history.isEmpty()) {
            sharedPreferences.edit().remove(historyKey).apply()
        } else {
            val json = gson.toJson(history)
            sharedPreferences.edit().putString(historyKey, json).apply()
        }
    }

    companion object {
        private const val MAX_HISTORY_SIZE = 10
    }
}