package com.example.posdromex.ui.screens.conversions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.posdromex.data.database.PosDatabase
import com.example.posdromex.data.database.entities.ConversionRule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConversionsViewModel(
    database: PosDatabase
) : ViewModel() {
    private val conversionRuleDao = database.conversionRuleDao()

    val conversions = conversionRuleDao.getAllConversionRules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addConversion(
        name: String,
        fromUnit: String,
        toUnit: String,
        operation: String,
        factor: Double,
        decimals: Int
    ) {
        viewModelScope.launch {
            val conversion = ConversionRule(
                name = name,
                fromUnit = fromUnit,
                toUnit = toUnit,
                operation = operation,
                factor = factor,
                decimals = decimals
            )
            conversionRuleDao.insert(conversion)
        }
    }

    fun updateConversion(conversion: ConversionRule) {
        viewModelScope.launch {
            conversionRuleDao.update(conversion)
        }
    }

    fun deleteConversion(conversion: ConversionRule) {
        viewModelScope.launch {
            conversionRuleDao.delete(conversion)
        }
    }
}

class ConversionsViewModelFactory(
    private val database: PosDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConversionsViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
