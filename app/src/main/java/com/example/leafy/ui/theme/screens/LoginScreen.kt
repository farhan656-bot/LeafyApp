package com.example.leafy.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.leafy.data.LeafyDatabase
import com.example.leafy.data.UserPreferences
import com.example.leafy.data.UserRepository
import com.example.leafy.ui.theme.LeafyGreen
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = remember { LeafyDatabase.getDatabase(context) }
    val repo = remember { UserRepository(db.userDao()) }
    val prefs = remember { UserPreferences(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = LeafyGreen,
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = LeafyGreen,
        cursorColor = LeafyGreen,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black
    )

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0))) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp).background(LeafyGreen),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("HELLO!", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Welcome to LEAFY", color = Color.White)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("LOGIN", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF333333))
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = LeafyGreen) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LeafyGreen) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        val ok = repo.loginUser(email.trim(), password)
                        if (ok) {
                            prefs.setLoggedIn(email.trim(), true)
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Email atau password salah", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LeafyGreen)
            ) { Text("Login", color = Color.White, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(24.dp))
            Row {
                Text("Don't have account? ", color = Color.DarkGray)
                TextButton(onClick = { navController.navigate("signup") }) {
                    Text("Sign Up", color = LeafyGreen, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
