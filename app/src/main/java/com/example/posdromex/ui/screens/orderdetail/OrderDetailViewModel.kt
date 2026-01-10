package com.example.posdromex.ui.screens.orderdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.posdromex.data.database.dao.AppSettingsDao
import com.example.posdromex.data.database.dao.CustomerDao
import com.example.posdromex.data.database.dao.DeliveryInfoDao
import com.example.posdromex.data.database.dao.SaleDao
import com.example.posdromex.data.database.dao.SaleItemDao
import com.example.posdromex.data.database.entities.AppSettings
import com.example.posdromex.data.database.entities.Customer
import com.example.posdromex.data.database.entities.DeliveryInfo
import com.example.posdromex.data.database.entities.Sale
import com.example.posdromex.data.database.entities.SaleItem
import com.example.posdromex.printer.BluetoothPrinterService
import com.example.posdromex.printer.ReceiptPrinter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderDetailViewModel(
    private val saleId: Long,
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao,
    private val customerDao: CustomerDao,
    private val deliveryInfoDao: DeliveryInfoDao,
    private val appSettingsDao: AppSettingsDao,
    private val printerService: BluetoothPrinterService,
    private val receiptPrinter: ReceiptPrinter
) : ViewModel() {

    private val _sale = MutableStateFlow<Sale?>(null)
    val sale: StateFlow<Sale?> = _sale.asStateFlow()

    private val _saleItems = MutableStateFlow<List<SaleItem>>(emptyList())
    val saleItems: StateFlow<List<SaleItem>> = _saleItems.asStateFlow()

    private val _customer = MutableStateFlow<Customer?>(null)
    val customer: StateFlow<Customer?> = _customer.asStateFlow()

    private val _deliveryInfo = MutableStateFlow<DeliveryInfo?>(null)
    val deliveryInfo: StateFlow<DeliveryInfo?> = _deliveryInfo.asStateFlow()

    val settings: StateFlow<AppSettings?> = appSettingsDao.getSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadOrderDetails()
    }

    private fun loadOrderDetails() {
        viewModelScope.launch {
            _isLoading.value = true

            // Load sale
            val loadedSale = saleDao.getSaleById(saleId)
            _sale.value = loadedSale

            // Load sale items
            _saleItems.value = saleItemDao.getItemsBySaleIdSync(saleId)

            // Load customer if exists
            loadedSale?.customerId?.let { customerId ->
                _customer.value = customerDao.getCustomerByIdSync(customerId)
            }

            // Load delivery info if exists
            _deliveryInfo.value = deliveryInfoDao.getDeliveryInfoBySaleId(saleId)

            _isLoading.value = false
        }
    }

    fun printReceipt() {
        viewModelScope.launch {
            val currentSale = _sale.value
            if (currentSale == null) {
                _message.value = "Order not loaded"
                return@launch
            }

            if (!printerService.isConnected()) {
                _message.value = "Printer not connected"
                return@launch
            }

            val currentSettings = settings.value ?: run {
                _message.value = "Settings not loaded"
                return@launch
            }

            val businessInfo = ReceiptPrinter.BusinessInfo(
                name = currentSettings.businessName,
                phone = currentSettings.businessPhone,
                location = currentSettings.businessLocation,
                footer = currentSettings.receiptFooter
            )

            // Check if this is a reprint (print count > 0)
            val isReprint = currentSale.receiptPrintCount > 0

            val result = receiptPrinter.printReceipt(
                sale = currentSale,
                items = _saleItems.value,
                customer = _customer.value,
                businessInfo = businessInfo,
                currency = currentSettings.defaultCurrency,
                isReprint = isReprint
            )

            if (result.isSuccess) {
                // Increment print count
                saleDao.incrementReceiptPrintCount(saleId)
                // Reload to update the print count in UI
                _sale.value = saleDao.getSaleById(saleId)

                val printType = if (isReprint) "COPY" else "ORIGINAL"
                _message.value = "Receipt printed ($printType): ${currentSale.documentNumber}"
            } else {
                _message.value = "Print failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun printDeliveryAuth() {
        viewModelScope.launch {
            val currentSale = _sale.value
            val currentDeliveryInfo = _deliveryInfo.value

            if (currentSale == null) {
                _message.value = "Order not loaded"
                return@launch
            }

            if (currentDeliveryInfo == null) {
                _message.value = "No delivery info for this order"
                return@launch
            }

            if (!printerService.isConnected()) {
                _message.value = "Printer not connected"
                return@launch
            }

            val currentSettings = settings.value ?: run {
                _message.value = "Settings not loaded"
                return@launch
            }

            val businessInfo = ReceiptPrinter.BusinessInfo(
                name = currentSettings.businessName,
                phone = currentSettings.businessPhone,
                location = currentSettings.businessLocation,
                footer = currentSettings.receiptFooter
            )

            val printerDeliveryInfo = ReceiptPrinter.DeliveryInfo(
                driverName = currentDeliveryInfo.driverName,
                truckPlate = currentDeliveryInfo.truckPlate,
                emptyWeight = currentDeliveryInfo.emptyWeight,
                fullWeight = currentDeliveryInfo.fullWeight,
                deliveryAddress = currentDeliveryInfo.deliveryAddress
            )

            // Check if this is a reprint (print count > 0)
            val isReprint = currentSale.deliveryAuthPrintCount > 0

            val result = receiptPrinter.printDeliveryAuthorization(
                sale = currentSale,
                items = _saleItems.value,
                customer = _customer.value,
                businessInfo = businessInfo,
                deliveryInfo = printerDeliveryInfo,
                isReprint = isReprint
            )

            if (result.isSuccess) {
                // Increment print count
                saleDao.incrementDeliveryAuthPrintCount(saleId)
                // Reload to update the print count in UI
                _sale.value = saleDao.getSaleById(saleId)

                val printType = if (isReprint) "COPY" else "ORIGINAL"
                _message.value = "Delivery Auth printed ($printType): ${currentSale.documentNumber}"
            } else {
                _message.value = "Print failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

class OrderDetailViewModelFactory(
    private val saleId: Long,
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao,
    private val customerDao: CustomerDao,
    private val deliveryInfoDao: DeliveryInfoDao,
    private val appSettingsDao: AppSettingsDao,
    private val printerService: BluetoothPrinterService,
    private val receiptPrinter: ReceiptPrinter
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderDetailViewModel(
                saleId, saleDao, saleItemDao, customerDao, deliveryInfoDao,
                appSettingsDao, printerService, receiptPrinter
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
