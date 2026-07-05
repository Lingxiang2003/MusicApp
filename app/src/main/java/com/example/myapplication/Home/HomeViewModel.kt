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
                Song("Superjeilezick", "Brings"),
                Song("Viva Colonia", "Hжhner"),
                Song("Gold", "Klee"),
                Song("Vitamin C", "Can"),
                Song("Profitgeier", "Floh de Cologne"),
                Song("Cologne Jazz Session", "WDR Big Band")
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
