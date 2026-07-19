package com.example.myapplication.Waypoints

data class Waypoint(
    val id: Long,
    val owner: String,
    val name: String,
    val description: String,
    val songTitle: String,
    val songArtist: String,
    val latitude: Double,
    val longitude: Double
)
