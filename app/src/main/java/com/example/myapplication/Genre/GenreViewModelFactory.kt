package com.example.myapplication.Genre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.UserPreferences

class GenreViewModelFactory(
    private val preferences: UserPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(
                GenreViewModel::class.java
            )
        ) {

            @Suppress("UNCHECKED_CAST")

            return GenreViewModel(
                preferences
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel"
        )
    }
}