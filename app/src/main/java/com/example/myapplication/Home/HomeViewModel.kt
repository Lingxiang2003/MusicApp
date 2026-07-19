package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.Home.HomeUiState
import com.example.myapplication.Home.Song
import com.example.myapplication.Home.musicCatalog
import com.example.myapplication.Recommendations.RecommendationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private var recommendationJob: Job? = null

    private val _uiState = MutableStateFlow(
        HomeUiState(
            queue = musicCatalog
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

    fun receiveRecommendation(song: Song) {
        _uiState.update { state ->
            if (state.receivedSong == song) {
                state
            } else {
                state.copy(
                    queue = state.queue + song,
                    receivedSong = song,
                    expanded = true
                )
            }
        }
    }

    fun dismissReceivedRecommendation() {
        _uiState.update { it.copy(receivedSong = null) }
    }

    fun startRecommendationUpdates(username: String) {
        if (recommendationJob?.isActive == true) return
        recommendationJob = viewModelScope.launch {
            var lastMessageId = 0L
            while (isActive) {
                try {
                    val messages = RecommendationRepository.waitForNext(username, lastMessageId)
                    messages.maxByOrNull { it.id }?.let { message ->
                        lastMessageId = message.id
                        _uiState.update { it.copy(incomingRecommendation = message) }
                    }
                } catch (_: Exception) {
                    delay(2_000)
                }
            }
        }
    }

    fun dismissIncomingRecommendation() {
        _uiState.update { it.copy(incomingRecommendation = null) }
    }

    fun playIncomingRecommendation() {
        val message = _uiState.value.incomingRecommendation ?: return
        val song = Song(message.title, message.artist)
        _uiState.update { state ->
            state.copy(
                currentSong = song,
                queue = if (song in state.queue) state.queue else state.queue + song,
                isPlaying = true,
                incomingRecommendation = null
            )
        }
    }

}
