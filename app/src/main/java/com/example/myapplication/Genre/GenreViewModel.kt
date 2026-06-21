package com.example.myapplication.Genre

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GenreViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        GenreUiState()
    )

    val uiState = _uiState.asStateFlow()

    fun toggleGenre(genre: String) {

        val currentGenres = _uiState.value.selectedGenres

        if (genre in currentGenres) {

            _uiState.value = _uiState.value.copy(
                selectedGenres = currentGenres - genre
            )

        } else {

            _uiState.value = _uiState.value.copy(
                selectedGenres = currentGenres + genre
            )
        }
    }
}