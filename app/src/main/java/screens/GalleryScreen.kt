package com.example.leafy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.leafy.data.LeafyDatabase
import com.example.leafy.data.PlantEntity
import com.example.leafy.data.PlantLogEntity
import com.example.leafy.ui.theme.DarkerGreen
import com.example.leafy.ui.theme.LeafyGreen
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { LeafyDatabase.getDatabase(context) }


    val plantsFlow = remember { db.plantDao().observeAllPlants() }
    val plants by plantsFlow.collectAsState(initial = emptyList())


    var selectedPlantId by remember { mutableStateOf<Int?>(null) }


    val logsFlow: Flow<List<PlantLogEntity>> = remember(selectedPlantId) {
        if (selectedPlantId == null) db.plantLogDao().observeAllLogs()
        else db.plantLogDao().observeLogsByPlantId(selectedPlantId!!)
    }
    val logs by logsFlow.collectAsState(initial = emptyList())


    val plantNameMap = remember(plants) { plants.associate { it.id to it.name } }

    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

    Scaffold(
        containerColor = LeafyGreen,
        topBar = {
            TopAppBar(
                title = { Text("Gallery Perawatan", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkerGreen)
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(LeafyGreen)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {


            PlantFilterDropdown(
                plants = plants,
                selectedPlantId = selectedPlantId,
                onPlantSelected = { selectedPlantId = it }
            )

            Spacer(Modifier.height(12.dp))

            if (logs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada dokumentasi perawatan.", color = Color.White)
                }
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(logs) { log ->
                    GalleryLogCard(
                        log = log,
                        plantName = plantNameMap[log.plantId] ?: "Tanaman",
                        dateText = dateFmt.format(Date(log.createdAt))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlantFilterDropdown(
    plants: List<PlantEntity>,
    selectedPlantId: Int?,
    onPlantSelected: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = plants.firstOrNull { it.id == selectedPlantId }?.name ?: "Semua Tanaman"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = LeafyGreen,
                focusedBorderColor = LeafyGreen,
                unfocusedBorderColor = Color.White
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Semua Tanaman") },
                onClick = {
                    onPlantSelected(null)
                    expanded = false
                }
            )
            plants.forEach { plant ->
                DropdownMenuItem(
                    text = { Text(plant.name) },
                    onClick = {
                        onPlantSelected(plant.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun GalleryLogCard(
    log: PlantLogEntity,
    plantName: String,
    dateText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Foto / placeholder
            if (!log.photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(log.photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto perawatan",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Nama tanaman (badge)
            Surface(
                color = DarkerGreen.copy(alpha = 0.12f),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = plantName,
                    color = DarkerGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(dateText, color = Color.Gray, style = MaterialTheme.typography.labelLarge)

            if (!log.note.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(log.note!!, color = Color.Black, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
