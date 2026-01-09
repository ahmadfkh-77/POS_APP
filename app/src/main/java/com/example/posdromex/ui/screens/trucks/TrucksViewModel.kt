package com.example.posdromex.ui.screens.trucks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.posdromex.data.database.PosDatabase
import com.example.posdromex.data.database.entities.Truck
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrucksViewModel(
    database: PosDatabase
) : ViewModel() {
    private val truckDao = database.truckDao()

    val trucks = truckDao.getAllTrucks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTruck(plateNumber: String, description: String?) {
        viewModelScope.launch {
            val truck = Truck(
                plateNumber = plateNumber,
                description = description?.takeIf { it.isNotBlank() }
            )
            truckDao.insert(truck)
        }
    }

    fun deleteTruck(truck: Truck) {
        viewModelScope.launch {
            truckDao.delete(truck)
        }
    }
}

class TrucksViewModelFactory(
    private val database: PosDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrucksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrucksViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
