package com.example.leafy.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.leafy.R
import com.example.leafy.data.LeafyDatabase
import com.example.leafy.data.PlantEntity
import com.example.leafy.data.UserPreferences
import com.example.leafy.ui.theme.LeafyGreen
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val db = remember { LeafyDatabase.getDatabase(context) }
    val prefs = remember { UserPreferences(context) }

    val plants by db.plantDao().observePlants().collectAsState(initial = emptyList())
    var userName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val storedName = prefs.getName()
        val emailName = prefs.getEmail()?.substringBefore("@")
        userName = storedName?.ifBlank { null } ?: emailName ?: "Leafy friend"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LeafyGreen,
        floatingActionButton = {
            AddPlantFAB(onClick = { navController.navigate("addPlant") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item { GreetingHeader(name = userName ?: "Leafy friend") }
            item { ReminderCard(count = calculatePlantsNeedWatering(plants)) }

            items(plants) { plant ->
                PlantCard(plant = plant) {
                    navController.navigate("plantDetail/${plant.id}")
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun GreetingHeader(name: String) {
    Text(
        text = "Hallo, $name",
        color = Color.White,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    )
}

@Composable
fun ReminderCard(count: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$count tanaman perlu disiram hari ini",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Lihat detail",
                tint = Color.Black
            )
        }
    }
}

@Composable
fun PlantCard(plant: PlantEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlantThumbnail(plant)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    plant.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    buildScheduleText(plant),
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    formatLastWateredLabel(plant.lastWatered),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun PlantThumbnail(plant: PlantEntity) {
    if (!plant.imageUri.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(plant.imageUri)
                .crossfade(true)
                .build(),
            contentDescription = plant.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.leafy),
            contentDescription = plant.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}

@Composable
fun AddPlantFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color.White,
        contentColor = Color.Black,
        shape = CircleShape
    ) {
        Icon(Icons.Filled.Add, "Tambah Tanaman")
    }
}

/* ----------------- Helper untuk hitung tanaman perlu disiram ----------------- */

private fun calculatePlantsNeedWatering(plants: List<PlantEntity>): Int {
    return plants.count { plant ->
        val interval = frequencyToIntervalDays(plant.waterFrequency)   // dari PlantUiUtils.kt
        if (interval == null) {
            true
        } else {
            val lastWatered = plant.lastWatered ?: 0L
            val daysAgo =
                TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastWatered).toInt()
            daysAgo >= interval
        }
    }
}
