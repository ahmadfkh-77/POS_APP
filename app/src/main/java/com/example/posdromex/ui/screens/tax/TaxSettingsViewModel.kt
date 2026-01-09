package com.example.posdromex.ui.screens.tax

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.posdromex.data.database.dao.AppSettingsDao
import com.example.posdromex.data.database.entities.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaxSettingsViewModel(
    private val appSettingsDao: AppSettingsDao
) : ViewModel() {

    val settings: StateFlow<AppSettings?> = appSettingsDao.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun updateTaxRate(taxRate: Double) {
        viewModelScope.launch {
            val current = settings.value ?: return@launch
            appSettingsDao.update(
                current.copy(defaultTaxRate = taxRate)
            )
            _message.value = "Tax rate saved"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

class TaxSettingsViewModelFactory(
    private val appSettingsDao: AppSettingsDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaxSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaxSettingsViewModel(appSettingsDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
