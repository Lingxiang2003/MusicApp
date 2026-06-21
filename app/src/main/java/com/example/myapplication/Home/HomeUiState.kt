package com.example.myapplication.Home

data class HomeUiState(
    val isPlaying: Boolean = true,
    val expanded: Boolean = false,
    val currentSong: Song = Song(
        title = "DOOMSDAY PT. 2",
        artist = "MockUp Marius"
    ),
    val queue: List<Song> = emptyList()
)