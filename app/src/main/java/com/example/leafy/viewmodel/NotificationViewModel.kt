package com.example.leafy.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leafy.LeafyApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(private val application: Application) : ViewModel() {
    private val dao = (application as LeafyApp).database.notificationDao()


    val notifications = dao.getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    fun markAllAsRead() {
        viewModelScope.launch {
            dao.markAllAsRead()
        }
    }
}

class NotificationViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationViewModel(application) as T
    }
}