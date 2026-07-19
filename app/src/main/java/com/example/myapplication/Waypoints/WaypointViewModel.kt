package com.example.myapplication.Waypoints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.myapplication.Home.Song
import com.example.myapplication.Home.musicCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WaypointUiState(
    val name: String = "",
    val description: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val selectedSong: Song = musicCatalog.first(),
    val waypoints: List<Waypoint> = emptyList(),
    val loading: Boolean = false,
    val status: String? = null
)

class WaypointViewModel(
    private val repository: WaypointRepository,
    private val currentUser: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(WaypointUiState(waypoints = repository.load()))
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value, status = null) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value) }
    fun updateLatitude(value: String) = _uiState.update { it.copy(latitude = value, status = null) }
    fun updateLongitude(value: String) = _uiState.update { it.copy(longitude = value, status = null) }
    fun selectSong(song: Song) = _uiState.update { it.copy(selectedSong = song, status = null) }

    fun useCurrentLocation(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(
                latitude = "%.6f".format(java.util.Locale.US, latitude),
                longitude = "%.6f".format(java.util.Locale.US, longitude),
                status = "Aktuelle Position übernommen"
            )
        }
    }

    fun save() {
        val state = _uiState.value
        val latitude = state.latitude.toDoubleOrNull()
        val longitude = state.longitude.toDoubleOrNull()
        if (state.name.isBlank() || latitude == null || longitude == null ||
            latitude !in -90.0..90.0 || longitude !in -180.0..180.0
        ) {
            _uiState.update { it.copy(status = "Bitte Name und gültige Koordinaten eingeben") }
            return
        }

        val waypoint = Waypoint(
            id = System.currentTimeMillis(),
            owner = currentUser,
            name = state.name.trim(),
            description = state.description.trim(),
            songTitle = state.selectedSong.title,
            songArtist = state.selectedSong.artist,
            latitude = latitude,
            longitude = longitude
        )
        repository.save(waypoint)
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, status = null) }
            runCatching { repository.publish(waypoint) }
                .onSuccess {
                    _uiState.update {
                        WaypointUiState(
                            waypoints = WaypointRepository.sharedWaypoints.value,
                            status = "Waypoint für alle Nutzer gespeichert"
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            waypoints = repository.load(),
                            status = "Lokal gespeichert, Backend nicht erreichbar"
                        )
                    }
                }
        }
    }

    fun delete(id: Long) {
        val waypoint = _uiState.value.waypoints.firstOrNull { it.id == id } ?: return
        if (!waypoint.owner.equals(currentUser, ignoreCase = true)) {
            _uiState.update { it.copy(status = "Nur der Ersteller kann diesen Waypoint löschen") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, status = null) }
            runCatching { repository.deleteShared(id, currentUser) }
                .onSuccess {
                    repository.delete(id)
                    _uiState.update {
                        it.copy(
                            loading = false,
                            waypoints = WaypointRepository.sharedWaypoints.value,
                            status = "Waypoint gelöscht"
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(loading = false, status = "Waypoint konnte nicht gelöscht werden")
                    }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, status = null) }
            runCatching { repository.refreshShared() }
                .onSuccess { points ->
                    _uiState.update { it.copy(loading = false, waypoints = points) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            waypoints = repository.load(),
                            status = "Backend nicht erreichbar"
                        )
                    }
                }
        }
    }
}

class WaypointViewModelFactory(
    private val repository: WaypointRepository,
    private val currentUser: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        WaypointViewModel(repository, currentUser) as T
}
