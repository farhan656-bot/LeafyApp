package com.example.leafy.screens

import android.widget.Toast
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
import com.example.leafy.data.LeafyDatabase
import com.example.leafy.data.UserRepository
import com.example.leafy.ui.theme.LeafyGreen
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { LeafyDatabase.getDatabase(context) }
    val repo = remember { UserRepository(db.userDao()) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

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
            modifier = Modifier.fillMaxWidth().height(120.dp).background(LeafyGreen),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White,
                    modifier = Modifier.size(24.dp).clickable { navController.popBackStack() })
                Spacer(Modifier.width(8.dp))
                Text("Back", color = Color.White, fontWeight = FontWeight.Medium)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sign Up", style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF333333)))
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nama") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = LeafyGreen) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors
            )

            Spacer(Modifier.height(16.dp))

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

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
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
                        if (name.isBlank()) {
                            Toast.makeText(context, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        if (password != confirmPassword) {
                            Toast.makeText(context, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        val ok = repo.registerUser(name.trim(), email.trim(), password)
                        if (ok) {
                            Toast.makeText(context, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
                            navController.navigate("login") { popUpTo("signup") { inclusive = true } }
                        } else {
                            Toast.makeText(context, "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LeafyGreen)
            ) { Text("Sign Up", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}