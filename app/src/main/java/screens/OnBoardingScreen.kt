package com.example.leafy.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.leafy.R
import com.example.leafy.ui.theme.LeafyGreen

@Composable
fun OnboardingScreen(navController: NavController, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize(), color = LeafyGreen) {
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 64.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Image(painter = painterResource(R.drawable.leafy), contentDescription = "Logo", modifier = Modifier.size(150.dp))
                Spacer(Modifier.height(16.dp))
                Text("LEAFY", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text("Grow better, Care smarter", fontSize = 16.sp, color = Color.White)
            }
            Button(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.width(280.dp).height(50.dp)
            ) { Text("Get Started", fontSize = 16.sp) }
        }
    }
}
