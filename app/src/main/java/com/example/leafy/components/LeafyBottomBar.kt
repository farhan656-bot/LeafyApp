package com.example.leafy.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.leafy.ui.theme.LeafyGreen

@Composable
fun LeafyBottomBar(navController: NavController, currentRoute: String) {
    NavigationBar(
        containerColor = Color.White
    ) {
        val items = listOf(
            BottomItem("home", Icons.Default.Home, "Home"),
            BottomItem("stats", Icons.Default.BarChart, "Stats"),
            BottomItem("gallery", Icons.Default.PhotoCamera, "Gallery"),
            BottomItem("profile", Icons.Default.Person, "Profile")
        )

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (currentRoute == item.route) LeafyGreen else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (currentRoute == item.route) LeafyGreen else Color.Gray
                    )
                }
            )
        }
    }
}

data class BottomItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)
