package com.example.myapplication

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import com.example.myapplication.communication.MusicRecommendation

@Composable
fun App(
    receivedRecommendation: MusicRecommendation? = null,
    onRecommendationConsumed: (MusicRecommendation) -> Unit = {}
) {
    // Scaffold = der allgemeine Aufbau der App
    Scaffold { paddingValues ->
        AppNavigation(
            modifier = Modifier.padding(paddingValues),
            receivedRecommendation = receivedRecommendation,
            onRecommendationConsumed = onRecommendationConsumed
        )
    }
}
