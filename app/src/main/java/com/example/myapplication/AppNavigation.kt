package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.Genre.GenreScreen
import com.example.myapplication.Home.HomeScreen
import com.example.myapplication.Login.LoginScreen
import com.example.myapplication.communication.MusicRecommendation
import com.example.myapplication.Recommendations.RecommendationsScreen
import com.example.myapplication.Home.musicCatalog
import com.example.myapplication.Waypoints.WaypointScreen

// Hier werden die Namen / Routen der Screens gespeichert
sealed class AppRoute(val route: String) {
    object Login : AppRoute("login")
    object Home : AppRoute("home")
    object Genres : AppRoute("genres")
    object Recommendations : AppRoute("recommendations")
    object Waypoints : AppRoute("waypoints")
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    receivedRecommendation: MusicRecommendation? = null,
    onRecommendationConsumed: (MusicRecommendation) -> Unit = {}
) {
    // Der NavController verwaltet die Navigation zwischen den Screens
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf("WilliWillsWissen") }

    // NavHost zeigt abhängig von der Route den passenden Screen an
    NavHost(
        navController = navController,
        startDestination = AppRoute.Login.route,
        modifier = modifier
    ) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginClick = { username ->
                    currentUser = username
                    navController.navigate(AppRoute.Home.route)
                }
            )
        }

        composable(AppRoute.Home.route) {
            HomeScreen(
                username = currentUser,
                receivedRecommendation = receivedRecommendation,
                onRecommendationConsumed = onRecommendationConsumed,
                onOpenRecommendations = {
                    navController.navigate(AppRoute.Recommendations.route)
                },
                onOpenWaypoints = {
                    navController.navigate(AppRoute.Waypoints.route)
                },
                onOpenGenres = {
                    navController.navigate(AppRoute.Genres.route)
                }
            )
        }

        composable(AppRoute.Recommendations.route) {
            RecommendationsScreen(
                currentUser = currentUser,
                songs = musicCatalog,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Waypoints.route) {
            WaypointScreen(
                currentUser = currentUser,
                onBackClick = { navController.popBackStack() }
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
