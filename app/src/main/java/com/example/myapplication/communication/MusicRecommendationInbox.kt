package com.example.myapplication.communication

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object MusicRecommendationInbox {
    private const val PREFERENCES = "music_recommendation_inbox"
    private const val KEY_TITLE = "title"
    private const val KEY_ARTIST = "artist"

    private val _recommendation = MutableStateFlow<MusicRecommendation?>(null)
    val recommendation = _recommendation.asStateFlow()

    fun initialize(context: Context) {
        if (_recommendation.value != null) return
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val title = preferences.getString(KEY_TITLE, null)
        val artist = preferences.getString(KEY_ARTIST, null)
        if (!title.isNullOrBlank() && !artist.isNullOrBlank()) {
            _recommendation.value = MusicRecommendation(title, artist)
        }
    }

    fun deliver(context: Context, recommendation: MusicRecommendation) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TITLE, recommendation.title)
            .putString(KEY_ARTIST, recommendation.artist)
            .apply()
        _recommendation.value = recommendation
    }

    fun consume(context: Context, recommendation: MusicRecommendation) {
        if (_recommendation.value != recommendation) return
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        _recommendation.value = null
    }
}
