package com.example.leafy.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.leafy.R
import com.example.leafy.ui.theme.LeafyGreen

@Composable
fun GalleryScreen(navController: NavController) {
    val photos = List(9) { R.drawable.leafy }
    var selectedFilter by remember { mutableStateOf("Semua Tanaman") }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LeafyGreen)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(Modifier.width(8.dp))
            Text("Back", color = Color.Black)
        }

        Spacer(Modifier.height(16.dp))

        // Filter dropdown
        Box {
            Button(onClick = { expanded = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                Text(selectedFilter, color = Color.Black)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("Semua Tanaman", "Kaktus", "Aloe Vera", "Lidah Mertua").forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            selectedFilter = it
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(photos) { imageRes ->
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Plant Photo",
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(Color.White)
                        .padding(4.dp)
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = { /* TODO: Add photo */ },
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Foto")
            }
        }
    }
}
