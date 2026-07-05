package com.example.myapplication.Genre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GenreViewModel(
    private val preferences: UserPreferences
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(GenreUiState())

    val uiState = _uiState.asStateFlow()

    init {

        viewModelScope.launch {

            preferences.selectedGenres.collect { genres ->

                _uiState.value =
                    _uiState.value.copy(
                        selectedGenres = genres
                    )
            }
        }
    }

    fun toggleGenre(
        genre: String
    ) {

        val currentGenres =
            _uiState.value.selectedGenres

        val newGenres =
            if (genre in currentGenres) {

                currentGenres - genre

            } else {

                currentGenres + genre
            }

        _uiState.value =
            _uiState.value.copy(
                selectedGenres = newGenres
            )

        viewModelScope.launch {

            preferences.saveGenres(newGenres)
        }
    }
}