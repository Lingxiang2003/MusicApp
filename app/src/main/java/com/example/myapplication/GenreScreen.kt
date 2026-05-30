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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreScreen(
    onBackClick: () -> Unit = {}
) {
    Text(
        text = "← Back",
        color = Color.Black,
        fontSize = 16.sp,
        modifier = Modifier.clickable { onBackClick() }
    )

    Spacer(modifier = Modifier.height(16.dp))
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
                    // Einfacher Zurück-Button ohne extra Icon-Dependency.
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

            GenreRow("REGGAE", "METAL", "POP")
            GenreRow("ROCK", "HIP HOP", "INDIE")
            GenreRow("CLASSICAL", "HOUSE")
            GenreRow("TECHNO", "AMBIENT")
            GenreRow("EXPERIMENTAL", "RNB")
            GenreRow("JAZZ", "SOUL", "POLKA")
            GenreRow("COUNTRY", "SCHLAGER")

            Spacer(modifier = Modifier.weight(1f))

            // Grauer Infoblock unten aus dem Wireframe.
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
fun GenreRow(vararg genres: String) {
    // Eine Zeile mit mehreren Genre-Wörtern.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        genres.forEach { genre ->
            Text(
                text = genre,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (genre == "METAL" || genre == "HIP HOP") {
                    Color(0xFFB23A3A)
                } else {
                    Color(0xFF333333)
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