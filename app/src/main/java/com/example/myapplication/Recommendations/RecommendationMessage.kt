package com.example.myapplication.Recommendations

data class RecommendationMessage(
    val id: Long,
    val sender: String,
    val recipient: String,
    val title: String,
    val artist: String
)
