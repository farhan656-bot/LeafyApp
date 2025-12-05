package com.example.leafy.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
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
import com.example.leafy.data.Notification // Pastikan nama entity Anda benar
import com.example.leafy.ui.theme.LeafyGreen
import com.example.leafy.viewmodel.NotificationViewModel
import com.example.leafy.viewmodel.NotificationViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NotificationScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as Application

    // 1. Panggil ViewModel
    val viewModel: NotificationViewModel = viewModel(factory = NotificationViewModelFactory(app))

    // 2. Ambil data asli dari Database
    val notifications by viewModel.notifications.collectAsState()

    // 3. Efek Otomatis: Tandai semua sudah dibaca saat layar dibuka
    // (Ini yang membuat angka di lonceng hilang)
    LaunchedEffect(Unit) {
        viewModel.markAllAsRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LeafyGreen)
            .padding(16.dp)
    ) {
        // --- HEADER ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White, // Saya ubah jadi putih agar kontras dengan hijau
                modifier = Modifier
                    .size(24.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(Modifier.width(8.dp))
            Text("Back", color = Color.White)
        }

        Spacer(Modifier.height(24.dp))
        Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
        Spacer(Modifier.height(16.dp))

        // --- DAFTAR NOTIFIKASI ASLI ---
        if (notifications.isEmpty()) {
            // Tampilan jika kosong
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada notifikasi", color = Color.White.copy(alpha = 0.7f))
            }
        } else {
            // List Notifikasi dari Database
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: Notification) {
    // Format tanggal: "05 Des 2025, 14:30"
    val dateString = remember(notification.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(notification.timestamp)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notif",
                tint = LeafyGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))

            Column {
                // Pesan Notifikasi (Contoh: "Mawar sudah dirawat...")
                Text(
                    text = notification.message,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Waktu Notifikasi
                Text(
                    text = dateString,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}