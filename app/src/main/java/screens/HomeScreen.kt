package com.example.leafy.screens

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.leafy.R
import com.example.leafy.data.LeafyDatabase
import com.example.leafy.data.PlantEntity
import com.example.leafy.data.UserPreferences
import com.example.leafy.ui.theme.LeafyGreen
import com.example.leafy.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as Application

    val db = remember { LeafyDatabase.getDatabase(context) }
    val prefs = remember { UserPreferences(context) }
    val plants by db.plantDao().observePlants().collectAsState(initial = emptyList())
    var userName by remember { mutableStateOf<String?>(null) }

    val homeViewModel: HomeViewModel = viewModel()
    val unreadCount by homeViewModel.unreadCount.collectAsState()

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) Toast.makeText(context, "Izin notifikasi ditolak", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val storedName = prefs.getName()
        val emailName = prefs.getEmail()?.substringBefore("@")
        userName = storedName?.ifBlank { null } ?: emailName ?: "Leafy Friend"
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
            item {
                HomeHeader(
                    name = userName ?: "Leafy Friend",
                    unreadCount = unreadCount,
                    navController = navController
                )
            }

            item {
                ReminderCard(count = calculatePlantsNeedWatering(plants))
            }

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
fun HomeHeader(name: String, unreadCount: Int, navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Good Morning,\n$name",
            color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp
        )

        IconButton(onClick = { navController.navigate("notification") }) {
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        Badge(containerColor = Color.Red) { Text("$unreadCount", color = Color.White) }
                    }
                }
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun ReminderCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (count > 0) "$count tanaman perlu perhatian hari ini" else "Semua tanaman sudah aman!",
                modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Color.Black
            )
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Black)
        }
    }
}

@Composable
fun PlantCard(plant: PlantEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!plant.imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(plant.imageUri).crossfade(true).build(),
                    contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                )
            } else {
                Image(painter = painterResource(id = R.drawable.leafy), contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(plant.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text(plant.schedule.ifBlank { "Jadwal belum diatur" }, fontSize = 14.sp, color = Color.DarkGray)


                val lastWateredText = if (plant.lastWatered == null || plant.lastWatered == 0L) {
                    "Belum pernah"
                } else {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(plant.lastWatered!!))
                }
                Text("Terakhir disiram: $lastWateredText", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AddPlantFAB(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick, containerColor = Color.White, contentColor = Color.Black, shape = CircleShape) {
        Icon(Icons.Filled.Add, contentDescription = "Tambah")
    }
}


private fun calculatePlantsNeedWatering(plants: List<PlantEntity>): Int {
    return plants.count { plant ->
        if (plant.lastWatered == null || plant.lastWatered == 0L) {
            true
        } else {
            val satuHariInMs = 24 * 60 * 60 * 1000L
            (System.currentTimeMillis() - plant.lastWatered!!) >= satuHariInMs
        }
    }
}