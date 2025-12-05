package com.example.leafy.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.leafy.data.UserPreferences
import com.example.leafy.ui.theme.LeafyGreen
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        email = prefs.getEmail() ?: ""
        name = prefs.getName() ?: ""
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        disabledBorderColor = Color.Gray,
        disabledTextColor = Color.Black,
        disabledLeadingIconColor = LeafyGreen,
        disabledLabelColor = Color.Gray
    )

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0))) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp).background(LeafyGreen).padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White,
                    modifier = Modifier.size(24.dp).clickable { navController.popBackStack() })
                Spacer(Modifier.width(8.dp))
                Text("Back", color = Color.White, fontWeight = FontWeight.Medium)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp)
        ) {
            Text("Info Account", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF333333))
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name, onValueChange = {}, enabled = false,
                label = { Text("Nama") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email, onValueChange = {}, enabled = false,
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = "********", onValueChange = {}, enabled = false,
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    scope.launch {
                        prefs.setLoggedIn(email, name, false)
                        prefs.clear()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = LeafyGreen)
            ) { Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(16.dp))
            Text("Leafy App v1.0.0", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
