package com.example.leafy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leafy.LeafyApp
import com.example.leafy.data.NotificationDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


// 1. Ubah ViewModel menjadi AndroidViewModel dan teruskan 'notificationDao' dari konstruktor
class HomeViewModel(private val notificationDao: NotificationDao) : ViewModel() {
    // 2. Hapus baris yang menyebabkan error. Gunakan 'notificationDao' dari konstruktor.
    // private val notificationDao = (application as LeafyApp).database.notificationDao() // <-- HAPUS BARIS INI

    val unreadCount: StateFlow<Int> = notificationDao.getUnreadCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0
        )
}

// Factory Anda sudah benar dalam mengambil DAO dari Application
class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // Akses database melalui application class untuk diinjeksikan ke ViewModel
            val dao = (application as LeafyApp).database.notificationDao()
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(dao) as T // <-- Pastikan dao diteruskan ke konstruktor
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
