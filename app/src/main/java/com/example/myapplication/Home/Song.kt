package com.example.myapplication.Home

data class Song(
    val title: String,
    val artist: String
)

val musicCatalog = listOf(
    Song("Superjeilezick", "Brings"),
    Song("Viva Colonia", "Höhner"),
    Song("Gold", "Klee"),
    Song("Vitamin C", "Can"),
    Song("Profitgeier", "Floh de Cologne"),
    Song("Cologne Jazz Session", "WDR Big Band")
)
