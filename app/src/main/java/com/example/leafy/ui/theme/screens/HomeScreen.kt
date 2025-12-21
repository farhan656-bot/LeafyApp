package com.example.leafy.ui.theme.screens

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.leafy.R
import com.example.leafy.data.LeafyDatabase
import com.example.leafy.data.PlantEntity
import com.example.leafy.ui.theme.LeafyGreen
import com.example.leafy.viewmodel.HomeViewModel
import com.example.leafy.viewmodel.HomeViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as Application

    // Setup Database
    val db = remember { LeafyDatabase.getDatabase(context) }
    var plants by remember { mutableStateOf(listOf<PlantEntity>()) }

    // Setup Notifikasi (Lonceng)
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(app))
    val unreadCount by homeViewModel.unreadCount.collectAsState()

    LaunchedEffect(Unit) {
        plants = db.plantDao().getAllPlants()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LeafyGreen,


        floatingActionButton = {
            AddPlantFAB(onClick = { navController.navigate("addPlant") })
        },


        bottomBar = { HomeBottomBar() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            item {
                HomeHeader(
                    name = "John",
                    navController = navController,
                    unreadCount = unreadCount
                )
            }

            item { ReminderCard(count = plants.size) }

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
fun HomeHeader(name: String, navController: NavController, unreadCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Good Morning,\n$name",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = { navController.navigate("notifications") }) {
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        Badge { Text("$unreadCount") }
                    }
                }
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notifikasi", tint = Color.White, modifier = Modifier.size(28.dp))
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
            Text(text = "$count tanaman terdaftar", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Color.Black)
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
            Image(painter = painterResource(id = R.drawable.leafy), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(plant.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text(plant.schedule, fontSize = 14.sp, color = Color.DarkGray)
                Text(plant.lastWatered, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun HomeBottomBar() {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Filled.Home, "Home") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = LeafyGreen))
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Filled.BarChart, "Stats", tint = Color.Gray) })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Filled.PhotoCamera, "Camera", tint = Color.Gray) })
    }
}

@Composable
fun AddPlantFAB(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick, containerColor = Color.White, contentColor = Color.Black, shape = CircleShape) {
        Icon(Icons.Filled.Add, "Tambah")
    }
}