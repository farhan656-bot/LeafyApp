package com.example.leafy.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import com.example.leafy.LeafyApp
import com.example.leafy.R
import com.example.leafy.data.LeafyDatabase
import com.example.leafy.data.Notification // Pastikan file Entity Anda bernama Notification.kt
import com.example.leafy.data.PlantEntity
import com.example.leafy.ui.theme.DarkerGreen
import com.example.leafy.ui.theme.LeafyGreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.leafy.data.CareHistory

// ==========================
//  NOTIFICATION FUNCTION
// ==========================
fun showSystemNotification(context: Context, title: String, message: String) {
    val channelId = "leafy_channel"
    val notificationId = System.currentTimeMillis().toInt()
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Leafy Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.leafy) // Pastikan gambar leafy ada di drawable
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    notificationManager.notify(notificationId, builder.build())

    // Simpan ke Database
    val app = context.applicationContext as? LeafyApp
    if (app != null) {
        val dao = app.database.notificationDao()
        CoroutineScope(Dispatchers.IO).launch {
            // PERBAIKAN: Menggunakan 'insert' agar sesuai dengan standar DAO
            dao.insert(
                Notification(
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )
        }
    }
}

// ==========================
//      SCREEN CONTENT
// ==========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(navController: NavController, plantId: Int) {
    val context = LocalContext.current
    val db = remember { LeafyDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    // Request Permission Notifikasi (Android 13+)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Izin diterima/ditolak */ }
    )

    SideEffect {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var plant by remember { mutableStateOf<PlantEntity?>(null) }

    LaunchedEffect(plantId) {
        plant = db.plantDao().getPlantById(plantId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
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
                    .verticalScroll(rememberScrollState()), // Agar bisa discroll
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Gambar Tanaman
                Image(
                    painter = painterResource(id = R.drawable.leafy),
                    contentDescription = currentPlant.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                )

                // Nama Tanaman
                Text(
                    text = currentPlant.name,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 24.dp)
                )

                // Kartu Info
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Jadwal: ${currentPlant.schedule}", fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Terakhir: ${currentPlant.lastWatered}", fontSize = 14.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TOMBOL 1: Tandai Sudah Dirawat
                Button(
                    onClick = {
                        scope.launch {
                            val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

                            // --- TAMBAHKAN BARIS INI ---
                            val currentTime = System.currentTimeMillis() // Definisi waktu sekarang
                            // ---------------------------

                            // Update Database
                            db.plantDao().updateLastWatered(currentPlant.id, today)
                            plant = currentPlant.copy(lastWatered = today)

                            // Tampilkan Notifikasi & Simpan ke DB Notifikasi
                            showSystemNotification(context, "Perawatan", "${currentPlant.name} sudah dirawat! 🌿")

                            // Simpan ke Riwayat
                            db.careHistoryDao().insertCareHistory(
                                CareHistory( // Bisa langsung CareHistory karena sudah diimport
                                    plantId = currentPlant.id,
                                    careTimestamp = currentTime, // Sekarang variabel ini dikenali
                                    careType = "Disiram"
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Tandai sudah dirawat", color = LeafyGreen, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // TOMBOL 2: Hapus Tanaman
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

                Spacer(modifier = Modifier.height(16.dp))

                // TOMBOL 3: LIHAT RIWAYAT (Outline Button)
                OutlinedButton(
                    onClick = {
                        // Navigasi ke halaman riwayat
                        navController.navigate("careHistory/${plantId}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    // Border putih agar terlihat di background hijau
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Lihat Riwayat Lengkap 📜", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}