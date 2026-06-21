package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreScreen(

    onBackClick: () -> Unit = {}
) {


    val selectedGenres = remember {
        mutableStateListOf("METAL", "HIP HOP")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Genres",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(
                            text = "←",
                            color = Color.Black,
                            fontSize = 20.sp
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {

            Text(
                text = "GENRES",
                color = Color(0xFFB23A3A),
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            GenreRow(selectedGenres, "REGGAE", "METAL", "POP")
            GenreRow(selectedGenres, "ROCK", "HIP HOP", "INDIE")
            GenreRow(selectedGenres, "CLASSICAL", "HOUSE")
            GenreRow(selectedGenres, "TECHNO", "AMBIENT")
            GenreRow(selectedGenres, "EXPERIMENTAL", "RNB")
            GenreRow(selectedGenres, "JAZZ", "SOUL", "POLKA")
            GenreRow(selectedGenres, "COUNTRY", "SCHLAGER")

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(
                        color = Color(0xFF555555),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Einstellungen,\nNutzerdaten etc.",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun GenreRow(
    selectedGenres: MutableList<String>,
    vararg genres: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        genres.forEach { genre ->

            val isSelected = genre in selectedGenres

            Text(
                text = genre,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) {
                    Color(0xFFB23A3A)
                } else {
                    Color(0xFF333333)
                },
                modifier = Modifier.clickable {
                    if (isSelected) {
                        selectedGenres.remove(genre)
                    } else {
                        selectedGenres.add(genre)
                    }
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(18.dp))
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
fun GenreScreenPreview() {
    MaterialTheme {
        GenreScreen()
    }
}