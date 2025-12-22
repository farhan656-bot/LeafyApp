package com.example.leafy.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.leafy.LeafyApp
import com.example.leafy.data.CareHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CareHistoryViewModel(
    application: Application,
    private val plantId: Int
) : ViewModel() {

    private val database = (application as LeafyApp).database
    private val careHistoryDao = database.careHistoryDao()
    private val plantDao = database.plantDao()


    val historyList: StateFlow<List<CareHistory>> = careHistoryDao.getHistoryByPlantId(plantId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )


    val groupedHistory: StateFlow<Map<String, List<CareHistory>>> = careHistoryDao.getHistoryByPlantId(plantId)
        .map { list ->
            list.groupBy { history ->

                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(history.careTimestamp))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyMap()
        )


    fun resetHistory() {
        viewModelScope.launch(Dispatchers.IO) {

            careHistoryDao.deleteHistoryByPlantId(plantId)


            plantDao.resetLastWatered(plantId)
        }
    }
}


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