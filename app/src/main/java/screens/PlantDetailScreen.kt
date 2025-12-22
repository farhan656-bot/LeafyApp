package com.example.leafy.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.leafy.LeafyApp
import com.example.leafy.MainActivity
import com.example.leafy.R
import com.example.leafy.data.*
import com.example.leafy.ui.theme.DarkerGreen
import com.example.leafy.ui.theme.LeafyGreen
import com.example.leafy.utils.createImageUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


fun showSystemNotification(context: Context, title: String, message: String) {
    val channelId = "leafy_channel_high_priority"
    val notificationId = System.currentTimeMillis().toInt()
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Leafy Updates", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.leafy)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    notificationManager.notify(notificationId, builder.build())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(navController: NavController, plantId: Int) {
    val context = LocalContext.current
    val db = remember { LeafyDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }


    val plantFlow = remember { db.plantDao().observePlantById(plantId) }
    val plant by plantFlow.collectAsState(initial = null)


    var showDocDialog by remember { mutableStateOf(false) }
    var careTypeSelected by remember { mutableStateOf("Disiram") }
    var noteText by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }


    val quotes = remember {
        listOf(
            "Tumbuh itu butuh waktu, sabar ya! ",
            "Setiap tetes air adalah kehidupan. Kerja bagus! ",
            "Rawatlah tanamanmu seperti kamu merawat impianmu. ",
            "Hari ini kamu sudah berbuat baik untuk bumi. ",
            "Tanaman bahagia, hati pemiliknya juga bahagia! "
        )
    }


    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) selectedPhotoUri = pendingCameraUri else pendingCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    SideEffect {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
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


                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CareButton(
                        label = "Disiram",
                        icon = Icons.Default.WaterDrop,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            careTypeSelected = "Disiram"
                            showDocDialog = true
                        }
                    )
                    CareButton(
                        label = "Dipupuk",
                        icon = Icons.Default.Spa,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            careTypeSelected = "Dipupuk"
                            showDocDialog = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))


                OutlinedButton(
                    onClick = { navController.navigate("careHistory/${plantId}") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Lihat Riwayat Lengkap ", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        scope.launch {
                            db.plantDao().deletePlant(currentPlant.id)
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))
                ) {
                    Text("Hapus tanaman", color = Color.White)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }


            if (showDocDialog) {
                AlertDialog(
                    onDismissRequest = { showDocDialog = false },
                    title = { Text("Dokumentasi $careTypeSelected") },
                    text = {
                        Column {
                            selectedPhotoUri?.let { uri ->
                                AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                Spacer(Modifier.height(8.dp))
                            }
                            Button(
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        val uri = createImageUri(context)
                                        pendingCameraUri = uri
                                        takePictureLauncher.launch(uri)
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Ambil Foto") }

                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                label = { Text("Catatan") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            scope.launch {
                                if (selectedPhotoUri == null) {
                                    Toast.makeText(context, "Foto wajib diambil", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                val now = System.currentTimeMillis()
                                val randomQuote = quotes.random()


                                db.plantLogDao().insertLog(PlantLogEntity(plantId = currentPlant.id, photoUri = selectedPhotoUri.toString(), note = noteText, createdAt = now))


                                if (careTypeSelected == "Disiram") {
                                    db.plantDao().updateLastWatered(currentPlant.id, now)
                                }


                                db.careHistoryDao().insertCareHistory(CareHistory(plantId = currentPlant.id, careTimestamp = now, careType = careTypeSelected))
                                db.notificationDao().insert(Notification(title = "${currentPlant.name} $careTypeSelected", message = randomQuote, timestamp = now, isRead = false))


                                showSystemNotification(context, "${currentPlant.name} $careTypeSelected", randomQuote)

                                showDocDialog = false
                                selectedPhotoUri = null
                                noteText = ""
                                navController.navigate("home")
                            }
                        }) { Text("Simpan") }
                    },
                    dismissButton = { TextButton(onClick = { showDocDialog = false }) { Text("Batal") } }
                )
            }
        }
    }
}

@Composable
fun CareButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = LeafyGreen)
            Text(label, color = LeafyGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PlantHeroImage(plant: PlantEntity) {
    val model = if (!plant.imageUri.isNullOrBlank()) plant.imageUri else R.drawable.leafy
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(model).crossfade(true).build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
    )
}

@Composable
private fun CareSummaryCard(plant: PlantEntity, dateFormatter: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Jadwal: ${plant.waterFrequency} ${plant.waterDay}", fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Terakhir: ${if (plant.lastWatered != null) dateFormatter.format(Date(plant.lastWatered!!)) else "Belum"}", color = Color.Gray, fontSize = 14.sp)


            val interval = when(plant.waterFrequency) {
                "1x sehari" -> 1; "2x sehari" -> 1; "1x seminggu" -> 7; else -> null
            }
            if (interval != null && plant.lastWatered != null) {
                val next = plant.lastWatered!! + TimeUnit.DAYS.toMillis(interval.toLong())
                Text(text = "Berikutnya: ${dateFormatter.format(Date(next))}", color = LeafyGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


private fun createImageUri(context: Context): Uri {
    val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val file = File(picturesDir, "LEAFY_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}