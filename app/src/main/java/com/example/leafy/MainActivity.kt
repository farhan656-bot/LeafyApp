package com.example.leafy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.leafy.data.UserPreferences
import com.example.leafy.components.LeafyBottomBar
import com.example.leafy.screens.*
import com.example.leafy.ui.theme.LeafyTheme
import kotlinx.coroutines.runBlocking
import com.example.leafy.screens.GalleryScreen


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔍 Cek apakah user sudah login via DataStore
        val prefs = UserPreferences(this)
        val isLoggedIn = runBlocking { prefs.isUserLoggedIn() }

        setContent {
            LeafyTheme {
                val navController = rememberNavController()
                val bottomRoutes = listOf("home", "stats", "gallery", "profile")
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

                Scaffold(
                    bottomBar = {
                        if (currentRoute in bottomRoutes) {
                            LeafyBottomBar(navController, currentRoute ?: "home")
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) "home" else "onboarding",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("onboarding") { OnboardingScreen(navController) }
                        composable("login") { LoginScreen(navController) }
                        composable("signup") { SignUpScreen(navController) }
                        composable("home") { HomeScreen(navController) }
                        composable("addPlant") { AddPlantScreen(navController) }
                        composable(
                            "editPlant/{plantId}",
                            arguments = listOf(navArgument("plantId") { type = NavType.IntType })
                        ) { backStack ->
                            val id = backStack.arguments?.getInt("plantId") ?: 0
                            AddPlantScreen(navController, id)
                        }
                        composable(
                            "plantDetail/{plantId}",
                            arguments = listOf(navArgument("plantId") { type = NavType.IntType })
                        ) { backStack ->
                            val id = backStack.arguments?.getInt("plantId") ?: 0
                            PlantDetailScreen(navController, id)
                        }
                        composable("profile") { ProfileScreen(navController) }
                        composable("stats") { StatisticsScreen(navController) }
                        composable("notification") { NotificationScreen(navController) }
                        composable("gallery") { GalleryScreen(navController) }
                    }
                }
            }
        }
    }
}
