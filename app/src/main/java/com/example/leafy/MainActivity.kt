package com.example.leafy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.leafy.components.LeafyBottomBar
import com.example.leafy.data.UserPreferences
import com.example.leafy.screens.* // Pastikan semua file Screen berada di package ini
import com.example.leafy.ui.screens.CareHistoryScreen
import com.example.leafy.ui.theme.LeafyTheme
import com.example.leafy.utils.NotificationUtils
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_NAV_ROUTE = "extra_nav_route"
    }

    private var pendingNavRoute by mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Mengambil rute dari intent jika dipicu notifikasi saat aplikasi berjalan
        pendingNavRoute = intent.getStringExtra(EXTRA_NAV_ROUTE)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationUtils.createNotificationChannel(this)
        NotificationUtils.getFCMToken()

        val prefs = UserPreferences(this)
        val isLoggedIn = runBlocking { prefs.isUserLoggedIn() }

        // Route dari notifikasi saat aplikasi benar-benar tertutup (cold start)
        val launchRoute = intent.getStringExtra(EXTRA_NAV_ROUTE)

        setContent {
            LeafyTheme {
                val navController = rememberNavController()

                // Handle navigasi otomatis dari notifikasi
                val routeToGo = pendingNavRoute
                LaunchedEffect(routeToGo) {
                    if (!routeToGo.isNullOrBlank()) {
                        navController.navigate(routeToGo) {
                            launchSingleTop = true
                        }
                        pendingNavRoute = null
                    }
                }

                val bottomRoutes = listOf("home", "stats", "gallery", "profile")
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        if (currentRoute in bottomRoutes) {
                            LeafyBottomBar(navController, currentRoute ?: "home")
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) (launchRoute ?: "home") else "onboarding",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // --- AUTH ---
                        composable("onboarding") { OnboardingScreen(navController) }
                        composable("login") { LoginScreen(navController) }
                        composable("signup") { SignUpScreen(navController) }

                        // --- HOME & ADD ---
                        composable("home") { HomeScreen(navController) }

                        composable("addPlant") {
                            AddPlantScreen(navController, plantId = null)
                        }

                        // --- DETAIL & EDIT ---
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

                        // --- UPDATE: FIX CRASH RIWAYAT PERAWATAN ---
                        // Menambahkan rute careHistory agar aplikasi tidak crash saat tombol ditekan
                        composable(
                            route = "careHistory/{plantId}",
                            arguments = listOf(navArgument("plantId") { type = NavType.IntType })
                        ) { backStack ->
                            val id = backStack.arguments?.getInt("plantId") ?: 0
                            CareHistoryScreen(navController, id)
                        }

                        // --- LAINNYA ---
                        composable("profile") { ProfileScreen(navController) }
                        composable("stats") { StatisticsScreen(navController) }
                        composable("gallery") { GalleryScreen(navController) }

                        // FIX: Menggunakan "notification" (Tunggal) sesuai pemanggilan di HomeScreen
                        composable("notification") { NotificationScreen(navController) }
                    }
                }
            }
        }
    }
}