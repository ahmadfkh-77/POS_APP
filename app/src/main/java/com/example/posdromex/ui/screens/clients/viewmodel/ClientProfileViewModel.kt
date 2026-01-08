package com.example.posdromex.ui.screens.clients.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.posdromex.data.database.PosDatabase
import com.example.posdromex.data.database.entities.Customer
import com.example.posdromex.data.database.entities.Sale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ClientProfileViewModel(
    customerId: Long,
    database: PosDatabase
) : ViewModel() {
    private val customerDao = database.customerDao()
    private val saleDao = database.saleDao()

    val customer = customerDao.getCustomerById(customerId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val receipts = saleDao.getSalesByTypeAndCustomer(customerId, "RECEIPT")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val deliveryAuthorizations = saleDao.getSalesByTypeAndCustomer(customerId, "DELIVERY_AUTH")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

class ClientProfileViewModelFactory(
    private val customerId: Long,
    private val database: PosDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClientProfileViewModel(customerId, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

