package com.example.posdromex.ui.screens.drivers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.posdromex.data.database.PosDatabase
import com.example.posdromex.data.database.entities.Driver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DriversViewModel(
    database: PosDatabase
) : ViewModel() {
    private val driverDao = database.driverDao()

    val drivers = driverDao.getAllDrivers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addDriver(name: String) {
        viewModelScope.launch {
            val driver = Driver(name = name)
            driverDao.insert(driver)
        }
    }

    fun deleteDriver(driver: Driver) {
        viewModelScope.launch {
            driverDao.delete(driver)
        }
    }
}

class DriversViewModelFactory(
    private val database: PosDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DriversViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DriversViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
