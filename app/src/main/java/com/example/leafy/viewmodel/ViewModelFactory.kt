package com.example.leafy.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.leafy.LeafyApp

class HomeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // Kita mengambil DAO dari database yang ada di LeafyApp
            val dao = (application as LeafyApp).database.notificationDao()

            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}