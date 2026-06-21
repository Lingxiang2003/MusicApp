package com.example.myapplication.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import com.example.myapplication.viewmodel.HomeViewModel


@Composable
fun HomeScreen(
    onOpenGenres: () -> Unit = {},
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            HomeHeader(
                onOpenGenres = onOpenGenres
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF4F4F4))
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEFEFEF))
            )

            MapMarker(
                title = "Queen",
                subtitle = "Rock, Pop, Indie",
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-20).dp, y = 40.dp)
            )

            MapMarker(
                title = "6ix9ine",
                subtitle = "Hip-Hop, Trap",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 190.dp, end = 36.dp)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 45.dp, y = 130.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            )

            MusicPlayer(
                isPlaying = uiState.isPlaying,
                expanded = uiState.expanded,
                currentSong = uiState.currentSong,
                queue = uiState.queue,
                onPlayPause = {
                    viewModel.togglePlayback()
                },
                onExpand = {
                    viewModel.toggleQueue()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun HomeHeader(
    onOpenGenres: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profilbild als Platzhalter
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Benutzername und kurzer Untertitel
        Column {
            Text(
                text = "WilliWillsWissen",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Metal-Lover",
                fontSize = 12.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Pfeil öffnet den Genre-Screen
        Text(
            text = "→",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.clickable {
                onOpenGenres()
            }
        )
    }
}

@Composable
fun MapMarker(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kreis als Platzhalter für Bild/Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun MusicPlayer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    expanded: Boolean,
    currentSong: Song,
    queue: List<Song>,
    onPlayPause: () -> Unit,
    onExpand: () -> Unit
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .height(if (expanded) 500.dp else 170.dp)
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(20.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            if (expanded) {

                Text(
                    text = "QUEUE",
                    color = Color(0xFFC93434),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(queue) { song ->
                        Text(
                            text = song.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }


            Text(
                text = "Currently playing...",
                color = Color.White,
                fontSize = 10.sp
            )

            Text(
                text = currentSong.artist,
                color = Color.Gray,
                fontSize = 12.sp
            )

            Text(
                text = currentSong.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(Color.White)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = if (isPlaying) "▶" else "∥",
                    color = Color(0xFFC93434),
                    fontSize = if (isPlaying) 30.sp else 34.sp,
                    modifier = Modifier.clickable {
                        onPlayPause()
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = if (expanded) "▼" else "▲",
                    color = Color.White,
                    fontSize = 26.sp,
                    modifier = Modifier.clickable {
                        onExpand()
                    }
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}