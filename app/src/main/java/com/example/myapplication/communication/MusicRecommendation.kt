package com.example.myapplication.communication

import android.content.Intent
import android.net.Uri
import com.example.myapplication.Home.Song
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val SHARE_SCHEME = "moco"
private const val SHARE_HOST = "recommendation"
private const val TEXT_PREFIX = "Musik-Tipp:"

data class MusicRecommendation(
    val title: String,
    val artist: String
) {
    fun toSong() = Song(title = title, artist = artist)
}

object MusicRecommendationCodec {
    fun createDeepLink(song: Song): Uri = Uri.parse(createDeepLinkText(song))

    private fun createDeepLinkText(song: Song): String {
        val title = URLEncoder.encode(song.title, StandardCharsets.UTF_8.name())
        val artist = URLEncoder.encode(song.artist, StandardCharsets.UTF_8.name())
        return "$SHARE_SCHEME://$SHARE_HOST?title=$title&artist=$artist"
    }

    fun createShareText(song: Song): String = buildString {
        appendLine("$TEXT_PREFIX ${song.title} - ${song.artist}")
        append("In MOCO öffnen: ${createDeepLinkText(song)}")
    }

    fun fromIntent(intent: Intent?): MusicRecommendation? {
        if (intent == null) return null

        return when (intent.action) {
            Intent.ACTION_VIEW -> fromUri(intent.data)
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
                fromText(text)
            }
            else -> null
        }
    }

    fun fromText(text: String): MusicRecommendation? {
        val recommendation = text.lineSequence()
            .firstOrNull { it.startsWith(TEXT_PREFIX, ignoreCase = true) }
            ?.substringAfter(':')
            ?.trim()

        if (recommendation != null) {
            val parts = recommendation.split(" - ", limit = 2)
            if (parts.size == 2) {
                create(parts[0], parts[1])?.let { return it }
            }
        }

        val uriText = text.lineSequence()
            .flatMap { it.splitToSequence(' ') }
            .firstOrNull { it.startsWith("$SHARE_SCHEME://") }
            ?.trim()

        return fromUri(uriText?.let(Uri::parse))
    }

    fun fromUri(uri: Uri?): MusicRecommendation? {
        if (uri?.scheme != SHARE_SCHEME || uri.host != SHARE_HOST) return null
        return create(
            title = uri.getQueryParameter("title").orEmpty(),
            artist = uri.getQueryParameter("artist").orEmpty()
        )
    }

    private fun create(title: String, artist: String): MusicRecommendation? {
        val cleanTitle = title.trim()
        val cleanArtist = artist.trim()
        if (cleanTitle.isBlank() || cleanArtist.isBlank()) return null
        return MusicRecommendation(cleanTitle, cleanArtist)
    }
}
