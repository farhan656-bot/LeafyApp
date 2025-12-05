package com.example.leafy.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leafy.LeafyApp
import com.example.leafy.data.CareHistory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CareHistoryViewModel(
    application: Application,
    private val plantId: Int
) : ViewModel() {

    private val dao = (application as LeafyApp).database.careHistoryDao()

    // State untuk menyimpan daftar riwayat
    // Mengubah Flow dari Room menjadi StateFlow secara efisien
    val historyList: StateFlow<List<CareHistory>> = dao.getHistoryForPlant(plantId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

}

// Factory agar kita bisa mengirim 'plantId' ke dalam ViewModel
class CareHistoryViewModelFactory(
    private val application: Application,
    private val plantId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CareHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CareHistoryViewModel(application, plantId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
