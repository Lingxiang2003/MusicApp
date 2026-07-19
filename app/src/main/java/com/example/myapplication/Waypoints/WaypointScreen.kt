package com.example.myapplication.Waypoints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.Home.rememberDeviceLocation
import com.example.myapplication.Home.rememberLocationPermission
import com.example.myapplication.Home.musicCatalog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width

@Composable
fun WaypointScreen(
    currentUser: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: WaypointViewModel = viewModel(
        factory = WaypointViewModelFactory(WaypointRepository(context), currentUser)
    )
    val uiState by viewModel.uiState.collectAsState()
    val location = rememberDeviceLocation(rememberLocationPermission())

    Scaffold(containerColor = Color.White) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                TextButton(onClick = onBackClick) { Text("← Zurück") }
                Text(
                    "WAYPOINTS",
                    color = Color(0xFFC93434),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Orte mit Koordinaten speichern", color = Color.Gray)
                Text("Ersteller: $currentUser", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF2F2F2), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        label = { Text("Name, z. B. Uni") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::updateDescription,
                        label = { Text("Beschreibung (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Musik auswählen", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(musicCatalog) { song ->
                            val selected = song == uiState.selectedSong
                            Column(
                                modifier = Modifier
                                    .width(150.dp)
                                    .background(
                                        if (selected) Color(0xFFC93434) else Color.White,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.selectSong(song) }
                                    .padding(10.dp)
                            ) {
                                Text(
                                    song.title,
                                    color = if (selected) Color.White else Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    song.artist,
                                    color = if (selected) Color.White else Color.Gray,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CoordinateField(
                            value = uiState.latitude,
                            onValueChange = viewModel::updateLatitude,
                            label = "Breitengrad",
                            modifier = Modifier.weight(1f)
                        )
                        CoordinateField(
                            value = uiState.longitude,
                            onValueChange = viewModel::updateLongitude,
                            label = "Längengrad",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    TextButton(
                        onClick = {
                            location?.let {
                                viewModel.useCurrentLocation(it.latitude, it.longitude)
                            }
                        },
                        enabled = location != null
                    ) {
                        Text(if (location == null) "Warte auf GPS…" else "Aktuelle Position verwenden")
                    }
                    Button(
                        onClick = viewModel::save,
                        enabled = !uiState.loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC93434)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Waypoint speichern")
                    }
                    uiState.status?.let { Text(it, modifier = Modifier.padding(top = 6.dp)) }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Öffentliche Waypoints",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    TextButton(onClick = viewModel::refresh, enabled = !uiState.loading) {
                        Text(if (uiState.loading) "Lädt…" else "Aktualisieren")
                    }
                }
            }

            items(uiState.waypoints, key = { it.id }) { waypoint ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF222222), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Von ${waypoint.owner}", color = Color(0xFFC93434), fontSize = 12.sp)
                        Text(waypoint.name, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "♫ ${waypoint.songTitle} - ${waypoint.songArtist}",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        if (waypoint.description.isNotBlank()) {
                            Text(waypoint.description, color = Color.LightGray)
                        }
                        Text(
                            "%.5f, %.5f".format(
                                java.util.Locale.US,
                                waypoint.latitude,
                                waypoint.longitude
                            ),
                            color = Color(0xFFC93434),
                            fontSize = 12.sp
                        )
                    }
                    if (waypoint.owner.equals(currentUser, ignoreCase = true)) {
                        TextButton(
                            onClick = { viewModel.delete(waypoint.id) },
                            enabled = !uiState.loading
                        ) {
                            Text("Löschen", color = Color(0xFFC93434))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun CoordinateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}
