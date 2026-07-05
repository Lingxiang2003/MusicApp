package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.Genre.GenreScreen
import com.example.myapplication.Home.HomeScreen
import com.example.myapplication.Login.LoginScreen

// Hier werden die Namen / Routen der Screens gespeichert
sealed class AppRoute(val route: String) {
    object Login : AppRoute("login")
    object Home : AppRoute("home")
    object Genres : AppRoute("genres")
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    // Der NavController verwaltet die Navigation zwischen den Screens
    val navController = rememberNavController()

    // NavHost zeigt abhängig von der Route den passenden Screen an
    NavHost(
        navController = navController,
        startDestination = AppRoute.Login.route,
        modifier = modifier
    ) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(AppRoute.Home.route)
                }
            )
        }

        composable(AppRoute.Home.route) {
            HomeScreen(
                onOpenGenres = {
                    navController.navigate(AppRoute.Genres.route)
                }
            )
        }

        composable(AppRoute.Genres.route) {
            GenreScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}