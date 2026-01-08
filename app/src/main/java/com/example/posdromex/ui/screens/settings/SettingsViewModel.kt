package com.example.posdromex.ui.screens.settings

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.posdromex.data.database.dao.AppSettingsDao
import com.example.posdromex.data.database.entities.AppSettings
import com.example.posdromex.printer.BluetoothPrinterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val appSettingsDao: AppSettingsDao,
    private val printerService: BluetoothPrinterService
) : ViewModel() {

    val settings: StateFlow<AppSettings?> = appSettingsDao.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices.asStateFlow()

    private val _printerStatus = MutableStateFlow<PrinterStatus>(PrinterStatus.Disconnected)
    val printerStatus: StateFlow<PrinterStatus> = _printerStatus.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadPairedDevices()
        checkPrinterConnection()
    }

    fun loadPairedDevices() {
        if (printerService.isBluetoothEnabled()) {
            _pairedDevices.value = printerService.getPairedDevices()
        } else {
            _message.value = "Bluetooth is not enabled"
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToPrinter(device: BluetoothDevice) {
        viewModelScope.launch {
            _printerStatus.value = PrinterStatus.Connecting
            val result = printerService.connect(device)
            if (result.isSuccess) {
                _printerStatus.value = PrinterStatus.Connected
                appSettingsDao.updatePrinter(device.address, device.name ?: "Unknown")
                _message.value = "Connected to ${device.name}"
            } else {
                _printerStatus.value = PrinterStatus.Error(result.exceptionOrNull()?.message ?: "Connection failed")
                _message.value = result.exceptionOrNull()?.message ?: "Connection failed"
            }
        }
    }

    fun disconnectPrinter() {
        printerService.disconnect()
        _printerStatus.value = PrinterStatus.Disconnected
        _message.value = "Printer disconnected"
    }

    fun printTestPage() {
        viewModelScope.launch {
            if (!printerService.isConnected()) {
                _message.value = "Printer not connected"
                return@launch
            }
            val result = printerService.printTestPage()
            if (result.isSuccess) {
                _message.value = "Test page printed!"
            } else {
                _message.value = result.exceptionOrNull()?.message ?: "Print failed"
            }
        }
    }

    fun updateBusinessInfo(
        name: String,
        phone: String,
        location: String,
        footer: String
    ) {
        viewModelScope.launch {
            val current = settings.value ?: return@launch
            appSettingsDao.update(
                current.copy(
                    businessName = name,
                    businessPhone = phone,
                    businessLocation = location,
                    receiptFooter = footer
                )
            )
            _message.value = "Settings saved"
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun checkPrinterConnection() {
        _printerStatus.value = if (printerService.isConnected()) {
            PrinterStatus.Connected
        } else {
            PrinterStatus.Disconnected
        }
    }

    sealed class PrinterStatus {
        object Disconnected : PrinterStatus()
        object Connecting : PrinterStatus()
        object Connected : PrinterStatus()
        data class Error(val message: String) : PrinterStatus()
    }
}

class SettingsViewModelFactory(
    private val appSettingsDao: AppSettingsDao,
    private val printerService: BluetoothPrinterService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(appSettingsDao, printerService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
