package com.example.leafy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.leafy.LeafyApp
import com.example.leafy.data.NotificationDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


class HomeViewModel(application: Application) : AndroidViewModel(application) {


    private val notificationDao: NotificationDao = (application as LeafyApp).database.notificationDao()

    val unreadCount: StateFlow<Int> = notificationDao.getUnreadCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0
        )
}

