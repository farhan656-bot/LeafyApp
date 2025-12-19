package com.example.leafy.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.leafy.R
import com.example.leafy.data.LeafyDatabase
import com.example.leafy.data.PlantEntity
import com.example.leafy.data.PlantLogEntity
import com.example.leafy.ui.theme.DarkerGreen
import com.example.leafy.ui.theme.LeafyGreen
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.example.leafy.utils.createImageUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(navController: NavController, plantId: Int) {
    val context = LocalContext.current
    val db = remember { LeafyDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val dateFormatter = remember { SimpleDateFormat("dd MMMM", Locale.getDefault()) }
    val plantFlow = remember { db.plantDao().observePlantById(plantId) }
    val plant by plantFlow.collectAsState(initial = null)

    // ====== STATE untuk popup dokumentasi ======
    var showDocDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // ====== Launcher kamera ======
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedPhotoUri = pendingCameraUri
        } else {
            pendingCameraUri = null
        }
    }

    // ====== Launcher permission kamera ======
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("editPlant/$plantId") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Tanaman", tint = Color.White)
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
                    label = "Tandai sudah dilakukan perawatan",
                    onClick = {
                        showDocDialog = true
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

            // ====== POPUP DOKUMENTASI (kamera + catatan) ======
            if (showDocDialog) {
                AlertDialog(
                    onDismissRequest = { showDocDialog = false },
                    title = { Text("Dokumentasi Perawatan") },
                    text = {
                        Column {
                            // Preview foto
                            selectedPhotoUri?.let { uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Preview foto",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.height(10.dp))
                            }

                            // Tombol kamera
                            Button(
                                onClick = {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (granted) {
                                        val uri = createImageUri(context)
                                        pendingCameraUri = uri
                                        takePictureLauncher.launch(uri)
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ambil Foto (Kamera)")
                            }

                            Spacer(Modifier.height(12.dp))

                            // Catatan (biar teksnya gak pudar)
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                label = { Text("Catatan (opsional)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = LeafyGreen,
                                    unfocusedBorderColor = Color.LightGray,
                                    cursorColor = LeafyGreen
                                )
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    val uriString = selectedPhotoUri?.toString()
                                    if (uriString.isNullOrBlank()) {
                                        Toast.makeText(context, "Foto wajib diambil", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }

                                    val now = System.currentTimeMillis()

                                    // 1) simpan log perkembangan (foto + catatan)
                                    db.plantLogDao().insertLog(
                                        PlantLogEntity(
                                            plantId = currentPlant.id,
                                            photoUri = uriString,
                                            note = noteText.trim().ifBlank { null },
                                            createdAt = now
                                        )
                                    )

                                    // 2) update lastWatered
                                    db.plantDao().updateLastWatered(currentPlant.id, now)

                                    Toast.makeText(context, "Dokumentasi tersimpan", Toast.LENGTH_SHORT).show()

                                    // reset state
                                    showDocDialog = false
                                    noteText = ""
                                    selectedPhotoUri = null
                                    pendingCameraUri = null

                                    // 3) arahkan ke home
                                    navController.navigate("home") {
                                        launchSingleTop = true
                                        popUpTo("home") { inclusive = false }
                                    }
                                }
                            }
                        ) { Text("Simpan") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDocDialog = false }) { Text("Batal") }
                    },
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    textContentColor = Color.Black
                )
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
            Text(
                text = buildScheduleText(plant),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatLastWateredLabel(plant.lastWatered),
                color = Color.Gray,
                fontSize = 14.sp
            )

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

private fun nextWateringDate(plant: PlantEntity, formatter: SimpleDateFormat): String? {
    val interval = frequencyToIntervalDays(plant.waterFrequency) ?: return null
    val base = plant.lastWatered ?: return null
    val next = base + TimeUnit.DAYS.toMillis(interval.toLong())
    return formatter.format(Date(next))
}

private fun createImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        ?: context.filesDir  // fallback kalau external null (jarang, tapi bisa)

    // pastikan folder ada
    if (!picturesDir.exists()) picturesDir.mkdirs()

    val file = File(picturesDir, "LEAFY_$timeStamp.jpg")

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}



