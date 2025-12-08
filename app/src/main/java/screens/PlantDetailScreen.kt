package com.example.leafy.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.leafy.ui.theme.DarkerGreen
import com.example.leafy.ui.theme.LeafyGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(navController: NavController, plantId: Int) {
    val context = LocalContext.current
    val db = remember { LeafyDatabase.getDatabase(context) }

    val dateFormatter = remember { SimpleDateFormat("dd MMMM", Locale.getDefault()) }
    val plantFlow = remember { db.plantDao().observePlantById(plantId) }
    val plant by plantFlow.collectAsState(initial = null)

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("editPlant/$plantId") }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Tanaman",
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
                PlantHeroImage(currentPlant)

                Text(
                    text = currentPlant.name,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 24.dp)
                )

                CareSummaryCard(currentPlant, dateFormatter)

                Spacer(modifier = Modifier.height(16.dp))

                CareActionButton(
                    label = "Tandai sudah disiram",
                    onClick = {
                        val now = System.currentTimeMillis()
                        scope.launch {
                            db.plantDao().updateLastWatered(currentPlant.id, now)
                            Toast.makeText(
                                context,
                                "Berhasil ditandai sudah disiram hari ini",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    containerColor = LeafyGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                CareActionButton(
                    label = "Hapus tanaman",
                    onClick = {
                        scope.launch {
                            db.plantDao().deletePlant(currentPlant.id)
                            Toast.makeText(context, "Tanaman dihapus", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    },
                    containerColor = Color(0xFFB00020)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PlantHeroImage(plant: PlantEntity) {
    if (!plant.imageUri.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(plant.imageUri)
                .crossfade(true)
                .build(),
            contentDescription = plant.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.leafy),
            contentDescription = plant.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
        )
    }
}

@Composable
private fun CareSummaryCard(plant: PlantEntity, dateFormatter: SimpleDateFormat) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // teks jadwal siram/pupuk dari helper
            Text(
                text = buildScheduleText(plant),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            // teks terakhir disiram dari helper
            Text(
                text = formatLastWateredLabel(plant.lastWatered),
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            plant.location?.takeIf { it.isNotBlank() }?.let {
                Text(text = "Lokasi: $it", color = Color.DarkGray)
                Spacer(modifier = Modifier.height(4.dp))
            }
            plant.notes?.takeIf { it.isNotBlank() }?.let {
                Text(text = "Catatan: $it", color = Color.DarkGray)
            }

            val nextWaterDate = nextWateringDate(plant, dateFormatter)
            if (nextWaterDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Penyiraman berikutnya: $nextWaterDate", color = LeafyGreen)
            }
        }
    }
}

@Composable
private fun CareActionButton(
    label: String,
    onClick: () -> Unit,
    containerColor: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        Text(label, color = Color.White)
    }
}

/**
 * Hitung tanggal penyiraman berikutnya berdasarkan frekuensi siram.
 * Helper frequencyToIntervalDays diambil dari PlantUiUtils.kt (satu package).
 */
private fun nextWateringDate(plant: PlantEntity, formatter: SimpleDateFormat): String? {
    val interval = frequencyToIntervalDays(plant.waterFrequency) ?: return null
    val base = plant.lastWatered ?: return null
    val next = base + TimeUnit.DAYS.toMillis(interval.toLong())
    return formatter.format(Date(next))
}
