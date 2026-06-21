package com.example.myapplication

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class GenreViewModel : ViewModel() {

    val selectedGenres = mutableStateListOf(
        "METAL",
        "HIP HOP"
    )

    fun toggleGenre(genre: String) {
        if (genre in selectedGenres) {
            selectedGenres.remove(genre)
        } else {
            selectedGenres.add(genre)
        }
    }
}