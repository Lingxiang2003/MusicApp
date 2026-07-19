package com.example.myapplication.Recommendations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object RecommendationRepository {
    private const val BASE_URL = "http://10.0.2.2:8080"

    suspend fun send(
        sender: String,
        recipient: String,
        title: String,
        artist: String
    ) = withContext(Dispatchers.IO) {
        val connection = openConnection("$BASE_URL/recommendations", "POST")
        val body = JSONObject()
            .put("sender", sender)
            .put("recipient", recipient)
            .put("title", title)
            .put("artist", artist)
            .toString()

        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.outputStream.bufferedWriter().use { it.write(body) }
        ensureSuccessful(connection)
        connection.disconnect()
    }

    suspend fun receive(recipient: String): List<RecommendationMessage> =
        withContext(Dispatchers.IO) {
            val encodedRecipient = URLEncoder.encode(recipient, Charsets.UTF_8.name())
            val connection = openConnection(
                "$BASE_URL/recommendations?recipient=$encodedRecipient",
                "GET"
            )
            ensureSuccessful(connection)
            val json = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
            connection.disconnect()

            buildList {
                for (index in 0 until json.length()) {
                    val item = json.getJSONObject(index)
                    add(
                        RecommendationMessage(
                            id = item.getLong("id"),
                            sender = item.getString("sender"),
                            recipient = item.getString("recipient"),
                            title = item.getString("title"),
                            artist = item.getString("artist")
                        )
                    )
                }
            }
        }

    suspend fun waitForNext(
        recipient: String,
        afterId: Long
    ): List<RecommendationMessage> = withContext(Dispatchers.IO) {
        val encodedRecipient = URLEncoder.encode(recipient, Charsets.UTF_8.name())
        val connection = openConnection(
            "$BASE_URL/recommendations/wait?recipient=$encodedRecipient&after=$afterId",
            "GET",
            readTimeout = 30_000
        )
        ensureSuccessful(connection)
        val json = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
        connection.disconnect()

        buildList {
            for (index in 0 until json.length()) {
                val item = json.getJSONObject(index)
                add(
                    RecommendationMessage(
                        id = item.getLong("id"),
                        sender = item.getString("sender"),
                        recipient = item.getString("recipient"),
                        title = item.getString("title"),
                        artist = item.getString("artist")
                    )
                )
            }
        }
    }

    private fun openConnection(
        url: String,
        method: String,
        readTimeout: Int = 4_000
    ): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 4_000
            this.readTimeout = readTimeout
        }

    private fun ensureSuccessful(connection: HttpURLConnection) {
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException("Server antwortet mit ${connection.responseCode}")
        }
    }
}
