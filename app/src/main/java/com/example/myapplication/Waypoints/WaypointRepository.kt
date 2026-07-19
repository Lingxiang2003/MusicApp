package com.example.myapplication.Waypoints

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class WaypointRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "waypoints",
        Context.MODE_PRIVATE
    )

    fun load(): List<Waypoint> {
        val json = JSONArray(preferences.getString(KEY_WAYPOINTS, "[]"))
        return buildList {
            for (index in 0 until json.length()) {
                val item = json.getJSONObject(index)
                add(
                    Waypoint(
                        id = item.getLong("id"),
                        owner = item.optString("owner", "Lokal"),
                        name = item.getString("name"),
                        description = item.optString("description"),
                        songTitle = item.optString("songTitle", "Unbekannter Song"),
                        songArtist = item.optString("songArtist", "Unbekannt"),
                        latitude = item.getDouble("latitude"),
                        longitude = item.getDouble("longitude")
                    )
                )
            }
        }
    }

    fun save(waypoint: Waypoint) {
        persist(load() + waypoint)
    }

    suspend fun publish(waypoint: Waypoint) = withContext(Dispatchers.IO) {
        val connection = openConnection("POST")
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        val body = JSONObject()
            .put("owner", waypoint.owner)
            .put("name", waypoint.name)
            .put("description", waypoint.description)
            .put("songTitle", waypoint.songTitle)
            .put("songArtist", waypoint.songArtist)
            .put("latitude", waypoint.latitude)
            .put("longitude", waypoint.longitude)
            .toString()
        connection.outputStream.bufferedWriter().use { it.write(body) }
        ensureSuccessful(connection)
        connection.disconnect()
        refreshShared()
    }

    suspend fun refreshShared(): List<Waypoint> = withContext(Dispatchers.IO) {
        val connection = openConnection("GET")
        ensureSuccessful(connection)
        val json = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
        connection.disconnect()
        val points = buildList {
            for (index in 0 until json.length()) {
                val item = json.getJSONObject(index)
                add(
                    Waypoint(
                        id = item.getLong("id"),
                        owner = item.getString("owner"),
                        name = item.getString("name"),
                        description = item.optString("description"),
                        songTitle = item.optString("songTitle", "Ohne Musik"),
                        songArtist = item.optString("songArtist", "Unbekannt"),
                        latitude = item.getDouble("latitude"),
                        longitude = item.getDouble("longitude")
                    )
                )
            }
        }
        _sharedWaypoints.value = points
        points
    }

    suspend fun deleteShared(id: Long, owner: String) = withContext(Dispatchers.IO) {
        val encodedOwner = URLEncoder.encode(owner, Charsets.UTF_8.name())
        val connection = (
            URL("$WAYPOINTS_URL/$id?owner=$encodedOwner").openConnection() as HttpURLConnection
            ).apply {
            requestMethod = "DELETE"
            connectTimeout = 4_000
            readTimeout = 4_000
        }
        ensureSuccessful(connection)
        connection.disconnect()
        refreshShared()
    }

    fun delete(id: Long) {
        persist(load().filterNot { it.id == id })
    }

    private fun persist(waypoints: List<Waypoint>) {
        val json = JSONArray()
        waypoints.forEach { waypoint ->
            json.put(
                JSONObject()
                    .put("id", waypoint.id)
                    .put("owner", waypoint.owner)
                    .put("name", waypoint.name)
                    .put("description", waypoint.description)
                    .put("songTitle", waypoint.songTitle)
                    .put("songArtist", waypoint.songArtist)
                    .put("latitude", waypoint.latitude)
                    .put("longitude", waypoint.longitude)
            )
        }
        preferences.edit().putString(KEY_WAYPOINTS, json.toString()).apply()
    }

    companion object {
        private const val KEY_WAYPOINTS = "saved_waypoints"
        private const val WAYPOINTS_URL = "http://10.0.2.2:8080/waypoints"

        val _sharedWaypoints = MutableStateFlow<List<Waypoint>>(emptyList())
        val sharedWaypoints = _sharedWaypoints.asStateFlow()
    }

    private fun openConnection(method: String): HttpURLConnection =
        (URL(WAYPOINTS_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 4_000
            readTimeout = 4_000
        }

    private fun ensureSuccessful(connection: HttpURLConnection) {
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException("Server antwortet mit ${connection.responseCode}")
        }
    }
}
