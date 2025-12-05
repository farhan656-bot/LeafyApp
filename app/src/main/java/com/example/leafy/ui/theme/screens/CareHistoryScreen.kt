package com.example.leafy.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.WaterDrop
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareHistoryScreen(navController: NavController, plantId: Int) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Panggil ViewModel dengan mengirim plantId
    val viewModel: CareHistoryViewModel = viewModel(
        factory = CareHistoryViewModelFactory(application, plantId)
    )

    // Ambil data list
    val historyList by viewModel.historyList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Perawatan", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LeafyGreen)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (historyList.isEmpty()) {
                // Tampilan jika kosong
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada riwayat. Yuk rawat tanamanmu!", color = Color.Gray)
                }
            } else {
                // Tampilan Daftar Riwayat
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(historyList) { history ->
                        CareHistoryCard(history)
                    }
                }
            }
        }
    }
}

@Composable
fun CareHistoryCard(history: CareHistory) {
    val dateString = remember(history.careTimestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(history.careTimestamp)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)) // Hijau muda pucat
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon berdasarkan tipe perawatan
            Icon(
                imageVector = if (history.careType == "water") Icons.Default.WaterDrop else Icons.Default.History,
                contentDescription = null,
                tint = LeafyGreen,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = if (history.careType == "water") "Disiram" else "Dipupuk",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = dateString,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}