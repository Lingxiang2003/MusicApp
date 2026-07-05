package com.example.myapplication.Home

data class HomeUiState(
    val isPlaying: Boolean = true,
    val expanded: Boolean = false,
    val currentSong: Song = Song(
        title = "Superjeilezick",
        artist = "Brings"
    ),
    val queue: List<Song> = emptyList()
)
