package com.example.myapplication.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_preferences")

class UserPreferences(
    private val context: Context
) {

    companion object {
        val SELECTED_GENRES =
            stringSetPreferencesKey("selected_genres")
    }

    val selectedGenres: Flow<List<String>> =
        context.dataStore.data.map {

            it[SELECTED_GENRES]?.toList()
                ?: emptyList()
        }

    suspend fun saveGenres(
        genres: List<String>
    ) {

        context.dataStore.edit {

            it[SELECTED_GENRES] =
                genres.toSet()
        }
    }
}