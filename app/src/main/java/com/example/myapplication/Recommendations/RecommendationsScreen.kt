package com.example.myapplication.Recommendations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.Home.Song

@Composable
fun RecommendationsScreen(
    currentUser: String,
    songs: List<Song>,
    onBackClick: () -> Unit
) {
    val viewModel: RecommendationsViewModel = viewModel(
        factory = RecommendationsViewModelFactory(currentUser)
    )
    val uiState by viewModel.uiState.collectAsState()
    var selectedSong by remember { mutableStateOf(songs.first()) }

    Scaffold(containerColor = Color.White) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            TextButton(onClick = onBackClick) { Text("← Zurück") }
            Text(
                text = "MUSIK-TIPPS",
                color = Color(0xFFC93434),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Angemeldet als $currentUser", color = Color.Gray)

            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF2F2F2), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text("Song auswählen", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(songs) { song ->
                        val selected = song == selectedSong
                        Column(
                            modifier = Modifier
                                .width(155.dp)
                                .background(
                                    if (selected) Color(0xFFC93434) else Color.White,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedSong = song }
                                .padding(12.dp)
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
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = uiState.recipient,
                    onValueChange = viewModel::updateRecipient,
                    label = { Text("Empfängername") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.send(selectedSong) },
                    enabled = !uiState.loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC93434)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Musik-Tipp senden")
                }
                uiState.status?.let { Text(it, modifier = Modifier.padding(top = 8.dp)) }
            }

            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Empfangen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = viewModel::refresh, enabled = !uiState.loading) {
                    Text(if (uiState.loading) "Lädt…" else "Aktualisieren")
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.messages, key = { it.id }) { message ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF222222), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Text("Von ${message.sender}", color = Color(0xFFC93434), fontWeight = FontWeight.Bold)
                        Text(message.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(message.artist, color = Color.LightGray)
                    }
                }
            }
        }
    }
}
