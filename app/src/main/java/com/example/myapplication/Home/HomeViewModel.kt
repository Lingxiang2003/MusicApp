package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.Home.HomeUiState
import com.example.myapplication.Home.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            queue = listOf(
                Song("Maiglöckchen blühen", "Unknown"),
                Song("Zartmann Party", "Unknown"),
                Song("Pöbeln am Gaunerplatz", "Unknown"),
                Song("Love is in the Air", "Unknown"),
                Song("Shababs Botten", "Unknown"),
                Song("Kirkstein in D major", "Unknown"),
                Song("Frankenstein's Theme", "Unknown")
            )
        )
    )

    val uiState = _uiState.asStateFlow()

    fun togglePlayback() {
        _uiState.update {
            it.copy(
                isPlaying = !it.isPlaying
            )
        }
    }

    fun toggleQueue() {
        _uiState.update {
            it.copy(
                expanded = !it.expanded
            )
        }
    }
}