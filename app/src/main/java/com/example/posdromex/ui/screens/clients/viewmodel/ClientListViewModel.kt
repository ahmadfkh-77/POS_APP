package com.example.posdromex.ui.screens.clients.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.posdromex.data.database.PosDatabase
import com.example.posdromex.data.database.entities.Customer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClientListViewModel(
    database: PosDatabase
) : ViewModel() {
    private val customerDao = database.customerDao()

    val customers = customerDao.getAllCustomers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addCustomer(name: String, phone: String?, address: String?, notes: String?) {
        viewModelScope.launch {
            val customer = Customer(
                name = name,
                phone = phone?.takeIf { it.isNotBlank() },
                address = address?.takeIf { it.isNotBlank() },
                notes = notes?.takeIf { it.isNotBlank() }
            )
            customerDao.insertCustomer(customer)
        }
    }
}

class ClientListViewModelFactory(
    private val database: PosDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientListViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

