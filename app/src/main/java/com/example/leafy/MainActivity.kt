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
import com.example.leafy.ui.components.LeafyBottomBar
import com.example.leafy.ui.screens.AddPlantScreen
import com.example.leafy.ui.screens.CareHistoryScreen
import com.example.leafy.ui.screens.GalleryScreen
import com.example.leafy.ui.screens.LoginScreen
import com.example.leafy.ui.screens.NotificationScreen
import com.example.leafy.ui.screens.OnboardingScreen
import com.example.leafy.ui.screens.PlantDetailScreen
import com.example.leafy.ui.screens.ProfileScreen
import com.example.leafy.ui.screens.SignUpScreen
import com.example.leafy.ui.screens.StatisticsScreen
import com.example.leafy.ui.theme.screens.HomeScreen
import com.example.leafy.ui.theme.LeafyTheme
import kotlinx.coroutines.runBlocking


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

                // Daftar rute yang menampilkan Bottom Bar
                val bottomRoutes = listOf("home", "stats", "gallery", "profile")

                // Cek rute saat ini untuk menyembunyikan/menampilkan Bottom Bar
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry.value?.destination?.route

                Scaffold(
                    bottomBar = {
                        // Tampilkan BottomBar hanya jika rute saat ini ada di daftar bottomRoutes
                        // Kita perlu logic 'contains' yang sedikit lebih pintar jika ada argumen,
                        // tapi untuk rute dasar ini sudah cukup.
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
                        // --- AUTH & ONBOARDING ---
                        composable("onboarding") { OnboardingScreen(navController) }
                        composable("login") { LoginScreen(navController) }
                        composable("signup") { SignUpScreen(navController) }

                        // --- UTAMA ---
                        composable("home") { HomeScreen(navController) }
                        composable("addPlant") { AddPlantScreen(navController) }

                        // --- DETAIL TANAMAN ---
                        composable(
                            "plantDetail/{plantId}",
                            arguments = listOf(navArgument("plantId") { type = NavType.IntType })
                        ) { backStack ->
                            val id = backStack.arguments?.getInt("plantId") ?: 0
                            PlantDetailScreen(navController, id)
                        }

                        // --- RIWAYAT PERAWATAN (BARU DITAMBAHKAN) ---
                        composable(
                            route = "careHistory/{plantId}",
                            arguments = listOf(navArgument("plantId") { type = NavType.IntType })
                        ) { backStack ->
                            val id = backStack.arguments?.getInt("plantId") ?: 0
                            // Memanggil layar CareHistoryScreen
                            CareHistoryScreen(navController, id)
                        }
                        // --------------------------------------------

                        // --- MENU BAWAH ---
                        composable("profile") { ProfileScreen(navController) }
                        composable("stats") { StatisticsScreen(navController) }
                        composable("notifications") { NotificationScreen(navController) }
                        composable("gallery") { GalleryScreen(navController) }
                    }
                }
            }
        }
    }
}
