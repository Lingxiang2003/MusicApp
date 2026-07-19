package com.example.myapplication.Recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.Home.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecommendationsUiState(
    val recipient: String = "",
    val messages: List<RecommendationMessage> = emptyList(),
    val loading: Boolean = false,
    val status: String? = null
)

class RecommendationsViewModel(
    private val currentUser: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun updateRecipient(value: String) {
        _uiState.update { it.copy(recipient = value.trimStart(), status = null) }
    }

    fun send(song: Song) {
        val recipient = _uiState.value.recipient.trim()
        if (recipient.isBlank()) {
            _uiState.update { it.copy(status = "Bitte Empfängername eingeben") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, status = null) }
            runCatching {
                RecommendationRepository.send(
                    sender = currentUser,
                    recipient = recipient,
                    title = song.title,
                    artist = song.artist
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(loading = false, status = "Musik-Tipp an $recipient gesendet")
                }
            }.onFailure {
                _uiState.update {
                    it.copy(loading = false, status = "Backend nicht erreichbar")
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, status = null) }
            runCatching { RecommendationRepository.receive(currentUser) }
                .onSuccess { messages ->
                    _uiState.update {
                        it.copy(loading = false, messages = messages, status = null)
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(loading = false, status = "Backend nicht erreichbar")
                    }
                }
        }
    }
}

class RecommendationsViewModelFactory(
    private val currentUser: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        RecommendationsViewModel(currentUser) as T
}
