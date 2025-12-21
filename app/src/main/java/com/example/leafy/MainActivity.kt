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
import com.example.leafy.screens.*
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
        setIntent(intent) // penting biar Activity.intent ikut update
        pendingNavRoute = intent.getStringExtra(EXTRA_NAV_ROUTE)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationUtils.createNotificationChannel(this)
        NotificationUtils.getFCMToken()

        val prefs = UserPreferences(this)
        val isLoggedIn = runBlocking { prefs.isUserLoggedIn() }

        // route dari notif saat cold start
        val launchRoute = intent.getStringExtra(EXTRA_NAV_ROUTE)

        setContent {
            LeafyTheme {
                val navController = rememberNavController()

                // handle klik notif saat app sudah jalan
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
                val currentRoute =
                    navController.currentBackStackEntryAsState().value?.destination?.route

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
