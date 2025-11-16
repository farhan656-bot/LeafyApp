package com.example.leafy.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.navigation.NavController
import com.example.leafy.R
import com.example.leafy.data.LeafyDatabase
import com.example.leafy.data.PlantEntity
import com.example.leafy.ui.theme.DarkerGreen
import com.example.leafy.ui.theme.LeafyGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(navController: NavController, plantId: Int) {
    val context = LocalContext.current
    val db = remember { LeafyDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }
    var plant by remember { mutableStateOf<PlantEntity?>(null) }

    LaunchedEffect(plantId) {
        plant = db.plantDao().getPlantById(plantId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkerGreen)
            )
        },
        containerColor = LeafyGreen
    ) { innerPadding ->
        plant?.let { currentPlant ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.leafy),
                    contentDescription = currentPlant.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                )

                Text(
                    text = currentPlant.name,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 24.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Jadwal perawatan: ${currentPlant.schedule}",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Terakhir dirawat: ${currentPlant.lastWatered}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // UPDATE lastWatered
                Button(
                    onClick = {
                        scope.launch {
                            val today = dateFormatter.format(Date())
                            db.plantDao().updateLastWatered(currentPlant.id, today)
                            plant = currentPlant.copy(lastWatered = today)
                            Toast.makeText(
                                context,
                                "Berhasil ditandai sudah dirawat hari ini",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LeafyGreen)
                ) {
                    Text("Tandai sudah dirawat", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // DELETE tanaman
                Button(
                    onClick = {
                        scope.launch {
                            db.plantDao().deletePlant(currentPlant.id)
                            Toast.makeText(context, "Tanaman dihapus", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))
                ) {
                    Text("Hapus tanaman", color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
