package com.example.myapplication.communication

import com.example.myapplication.Home.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MusicRecommendationCodecTest {
    @Test
    fun shareTextCanBeReadAgain() {
        val song = Song("Viva Colonia", "Höhner")

        val result = MusicRecommendationCodec.fromText(
            MusicRecommendationCodec.createShareText(song)
        )

        assertEquals(MusicRecommendation(song.title, song.artist), result)
    }

    @Test
    fun unrelatedTextIsIgnored() {
        assertNull(MusicRecommendationCodec.fromText("Hallo aus Köln"))
    }
}
