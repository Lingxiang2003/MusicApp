package com.example.myapplication.Login

data class LoginUiState(
    val username: String = "",
    val password: String = ""
) {
    val loginEnabled: Boolean
        get() = username.isNotBlank() && password.isNotBlank()
}