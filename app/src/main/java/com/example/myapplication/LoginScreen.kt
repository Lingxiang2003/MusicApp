package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginEnabled = username.isNotBlank() && password.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(Color(0xFFF2F2F2))
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "PLACEHOLDER",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "NAME",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC93434)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "SIGN IN",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        LoginInputBox(
            value = username,
            onValueChange = { username = it },
            label = "Username"
        )

        Spacer(modifier = Modifier.height(16.dp))

        LoginPasswordBox(
            value = password,
            onValueChange = { password = it },
            label = "Password"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onLoginClick,
            enabled = loginEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC93434)
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("LOGIN")
        }

        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "or register with",
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            RegisterButton("Spotify")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            RegisterButton("Google")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            RegisterButton("Apple")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "terms of use",
            fontSize = 12.sp,
            color = Color(0xFF6F8FC9),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun LoginInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun LoginPasswordBox(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun RegisterButton(text: String) {
    Box(
        modifier = Modifier
            .width(230.dp)
            .height(42.dp)
            .background(
                color = Color(0xFF555555),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}