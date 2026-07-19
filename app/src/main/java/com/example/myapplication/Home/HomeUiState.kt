package com.example.myapplication.Home

import com.example.myapplication.Recommendations.RecommendationMessage

data class HomeUiState(
    val isPlaying: Boolean = true,
    val expanded: Boolean = false,
    val currentSong: Song = Song(
        title = "Superjeilezick",
        artist = "Brings"
    ),
    val queue: List<Song> = emptyList(),
    val receivedSong: Song? = null,
    val incomingRecommendation: RecommendationMessage? = null
)
