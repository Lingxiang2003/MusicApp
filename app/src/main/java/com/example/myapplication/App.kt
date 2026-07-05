package com.example.myapplication

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding

@Composable
fun App() {
    // Scaffold = der allgemeine Aufbau der App
    Scaffold { paddingValues ->
        AppNavigation(
            modifier = Modifier.padding(paddingValues)
        )
    }
}