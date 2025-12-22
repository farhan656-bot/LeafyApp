package com.example.leafy.ui.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.leafy.data.CareHistory
import com.example.leafy.ui.theme.LeafyGreen
import com.example.leafy.viewmodel.CareHistoryViewModel
import com.example.leafy.viewmodel.CareHistoryViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CareHistoryScreen(navController: NavController, plantId: Int) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewModel: CareHistoryViewModel = viewModel(
        factory = CareHistoryViewModelFactory(application, plantId)
    )

    // PERBAIKAN: Mengambil data yang sudah dikelompokkan dari ViewModel
    val groupedHistory by viewModel.groupedHistory.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Reset Progress?", fontWeight = FontWeight.Bold) },
            text = { Text("Menghapus riwayat akan meriset tanda 'Terakhir Disiram' pada tanaman ini. Lanjutkan?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetHistory()
                        showDeleteDialog = false
                        Toast.makeText(context, "Progres berhasil direset", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Reset Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Perawatan", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    // Cek jika ada data di dalam Map
                    if (groupedHistory.isNotEmpty()) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Reset", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LeafyGreen)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        if (groupedHistory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Belum ada riwayat perawatan.", color = Color.Gray, fontSize = 16.sp)
                }
            }
        } else {
            //
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp)
            ) {

                groupedHistory.forEach { (date, logs) ->
                    // 1. HEADER TANGGAL (Sticky)
                    stickyHeader {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF5F5F5) // Seragam dengan background
                        ) {
                            Text(
                                text = date,
                                modifier = Modifier.padding(vertical = 12.dp),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = LeafyGreen
                            )
                        }
                    }

                    // 2. DAFTAR ITEM PADA TANGGAL TERSEBUT
                    items(logs) { history ->
                        CareHistoryCard(history)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Jarak bawah agar tidak tertutup sistem navigasi
                item { Spacer(modifier = Modifier.height(50.dp)) }
            }
        }
    }
}

@Composable
fun CareHistoryCard(history: CareHistory) {
    // Format Jam
    val timeString = remember(history.careTimestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(history.careTimestamp)
    }

    val (icon, iconColor, titleText) = when (history.careType) {
        "Disiram" -> Triple(Icons.Default.WaterDrop, Color(0xFF2196F3), "Disiram Air")
        "Dipupuk" -> Triple(Icons.Default.Spa, Color(0xFF4CAF50), "Diberi Pupuk")
        else -> Triple(Icons.Default.History, Color.Gray, history.careType)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(titleText, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text("Jam $timeString WIB", color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}