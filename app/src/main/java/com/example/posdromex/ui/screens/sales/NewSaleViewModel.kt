package com.example.posdromex.ui.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.posdromex.data.database.dao.AppSettingsDao
import com.example.posdromex.data.database.dao.CustomerDao
import com.example.posdromex.data.database.dao.DeliveryInfoDao
import com.example.posdromex.data.database.dao.ItemDao
import com.example.posdromex.data.database.dao.SaleDao
import com.example.posdromex.data.database.dao.SaleItemDao
import com.example.posdromex.data.database.entities.AppSettings
import com.example.posdromex.data.database.entities.Customer
import com.example.posdromex.data.database.entities.DeliveryInfo
import com.example.posdromex.data.database.entities.Item
import com.example.posdromex.data.database.entities.Sale
import com.example.posdromex.data.database.entities.SaleItem
import com.example.posdromex.printer.BluetoothPrinterService
import com.example.posdromex.printer.ReceiptPrinter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartItem(
    val name: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val conversionRule: String? = null
) {
    val total: Double get() = quantity * unitPrice
}

data class DeliveryInfoInput(
    val driverName: String = "",
    val truckPlate: String = "",
    val emptyWeight: String = "",
    val fullWeight: String = "",
    val deliveryAddress: String = ""
) {
    fun hasData(): Boolean = driverName.isNotBlank() || truckPlate.isNotBlank() ||
            emptyWeight.isNotBlank() || fullWeight.isNotBlank() || deliveryAddress.isNotBlank()
}

// Singleton to persist cart state across navigation
object CurrentSaleState {
    var cartItems: List<CartItem> = emptyList()
    var selectedCustomer: Customer? = null
    var deliveryInfo: DeliveryInfoInput = DeliveryInfoInput()
    var printBothDocuments: Boolean = true

    fun clear() {
        cartItems = emptyList()
        selectedCustomer = null
        deliveryInfo = DeliveryInfoInput()
        printBothDocuments = true
    }
}

class NewSaleViewModel(
    private val customerDao: CustomerDao,
    private val itemDao: ItemDao,
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao,
    private val deliveryInfoDao: DeliveryInfoDao,
    private val appSettingsDao: AppSettingsDao,
    private val printerService: BluetoothPrinterService,
    private val receiptPrinter: ReceiptPrinter
) : ViewModel() {

    val customers: StateFlow<List<Customer>> = customerDao.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val items: StateFlow<List<Item>> = itemDao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings?> = appSettingsDao.getSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _selectedCustomer = MutableStateFlow<Customer?>(CurrentSaleState.selectedCustomer)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(CurrentSaleState.cartItems)
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _deliveryInfo = MutableStateFlow(CurrentSaleState.deliveryInfo)
    val deliveryInfo: StateFlow<DeliveryInfoInput> = _deliveryInfo.asStateFlow()

    private val _printBothDocuments = MutableStateFlow(CurrentSaleState.printBothDocuments)
    val printBothDocuments: StateFlow<Boolean> = _printBothDocuments.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val subtotal: Double
        get() = _cartItems.value.sumOf { it.total }

    fun selectCustomer(customer: Customer) {
        _selectedCustomer.value = customer
        CurrentSaleState.selectedCustomer = customer
    }

    fun addToCart(item: Item, quantity: Double, unit: String) {
        val cartItem = CartItem(
            name = item.name,
            quantity = quantity,
            unit = unit,
            unitPrice = item.price
        )
        _cartItems.value = _cartItems.value + cartItem
        CurrentSaleState.cartItems = _cartItems.value
    }

    fun addManualItem(name: String, quantity: Double, unit: String, price: Double) {
        val cartItem = CartItem(
            name = name,
            quantity = quantity,
            unit = unit,
            unitPrice = price
        )
        _cartItems.value = _cartItems.value + cartItem
        CurrentSaleState.cartItems = _cartItems.value
    }

    fun removeFromCart(index: Int) {
        _cartItems.value = _cartItems.value.toMutableList().apply { removeAt(index) }
        CurrentSaleState.cartItems = _cartItems.value
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedCustomer.value = null
        _deliveryInfo.value = DeliveryInfoInput()
        _printBothDocuments.value = true
        CurrentSaleState.clear()
    }

    fun updateDeliveryInfo(info: DeliveryInfoInput) {
        _deliveryInfo.value = info
        CurrentSaleState.deliveryInfo = info
    }

    fun togglePrintBothDocuments() {
        _printBothDocuments.value = !_printBothDocuments.value
        CurrentSaleState.printBothDocuments = _printBothDocuments.value
    }

    fun printSale() {
        viewModelScope.launch {
            if (!printerService.isConnected()) {
                _message.value = "Printer not connected"
                return@launch
            }

            val currentSettings = settings.value ?: run {
                _message.value = "Settings not loaded"
                return@launch
            }

            val cartList = _cartItems.value
            if (cartList.isEmpty()) {
                _message.value = "Cart is empty"
                return@launch
            }

            val customer = _selectedCustomer.value
            val delivery = _deliveryInfo.value
            val shouldPrintBoth = _printBothDocuments.value && delivery.hasData()
            val currentTime = System.currentTimeMillis()

            // Generate receipt document number
            val receiptDocNumber = "${currentSettings.receiptPrefix}${currentSettings.nextReceiptNumber.toString().padStart(6, '0')}"

            // Create receipt sale record
            val receiptSale = Sale(
                customerId = customer?.id,
                type = "RECEIPT",
                documentNumber = receiptDocNumber,
                date = currentTime,
                subtotal = subtotal,
                tax = 0.0,
                discount = 0.0,
                total = subtotal,
                status = "CASH"
            )

            val receiptSaleId = saleDao.insertSale(receiptSale)

            // Create sale items for receipt
            val saleItems = cartList.map { cartItem ->
                SaleItem(
                    saleId = receiptSaleId,
                    productName = cartItem.name,
                    quantity = cartItem.quantity,
                    unit = cartItem.unit,
                    unitPrice = cartItem.unitPrice,
                    total = cartItem.total,
                    conversionRuleName = cartItem.conversionRule
                )
            }
            saleItemDao.insertItems(saleItems)

            // Save delivery info if provided
            if (delivery.hasData()) {
                val deliveryInfoEntity = DeliveryInfo(
                    saleId = receiptSaleId,
                    driverName = delivery.driverName,
                    truckPlate = delivery.truckPlate,
                    emptyWeight = delivery.emptyWeight.toDoubleOrNull() ?: 0.0,
                    fullWeight = delivery.fullWeight.toDoubleOrNull() ?: 0.0,
                    deliveryAddress = delivery.deliveryAddress
                )
                deliveryInfoDao.insert(deliveryInfoEntity)
            }

            // Increment receipt number
            appSettingsDao.incrementReceiptNumber()

            // Prepare for printing
            val savedReceiptSale = saleDao.getSaleById(receiptSaleId) ?: receiptSale.copy(id = receiptSaleId)
            val savedItems = saleItemDao.getItemsBySaleIdSync(receiptSaleId)

            val businessInfo = ReceiptPrinter.BusinessInfo(
                name = currentSettings.businessName,
                phone = currentSettings.businessPhone,
                location = currentSettings.businessLocation,
                footer = currentSettings.receiptFooter
            )

            // Print receipt
            val receiptResult = receiptPrinter.printReceipt(
                sale = savedReceiptSale,
                items = savedItems,
                customer = customer,
                businessInfo = businessInfo,
                currency = currentSettings.defaultCurrency
            )

            if (receiptResult.isFailure) {
                _message.value = "Receipt print failed: ${receiptResult.exceptionOrNull()?.message}"
                return@launch
            }

            // If combined printing enabled and delivery info exists, also print delivery authorization
            if (shouldPrintBoth) {
                delay(1000) // Wait 1 second between prints for printer to be ready

                // Generate delivery auth document number
                val deliveryDocNumber = "${currentSettings.deliveryAuthPrefix}${currentSettings.nextDeliveryAuthNumber.toString().padStart(6, '0')}"

                // Create delivery auth sale record
                val deliveryAuthSale = Sale(
                    customerId = customer?.id,
                    type = "DELIVERY_AUTH",
                    documentNumber = deliveryDocNumber,
                    date = currentTime,
                    subtotal = subtotal,
                    tax = 0.0,
                    discount = 0.0,
                    total = subtotal,
                    status = "CASH"
                )

                val deliveryAuthSaleId = saleDao.insertSale(deliveryAuthSale)

                // Create sale items for delivery auth
                val deliveryAuthItems = cartList.map { cartItem ->
                    SaleItem(
                        saleId = deliveryAuthSaleId,
                        productName = cartItem.name,
                        quantity = cartItem.quantity,
                        unit = cartItem.unit,
                        unitPrice = cartItem.unitPrice,
                        total = cartItem.total,
                        conversionRuleName = cartItem.conversionRule
                    )
                }
                saleItemDao.insertItems(deliveryAuthItems)

                // Increment delivery auth number
                appSettingsDao.incrementDeliveryAuthNumber()

                val savedDeliveryAuthSale = saleDao.getSaleById(deliveryAuthSaleId) ?: deliveryAuthSale.copy(id = deliveryAuthSaleId)
                val savedDeliveryAuthItems = saleItemDao.getItemsBySaleIdSync(deliveryAuthSaleId)

                val printerDeliveryInfo = ReceiptPrinter.DeliveryInfo(
                    driverName = delivery.driverName,
                    truckPlate = delivery.truckPlate,
                    emptyWeight = delivery.emptyWeight.toDoubleOrNull() ?: 0.0,
                    fullWeight = delivery.fullWeight.toDoubleOrNull() ?: 0.0,
                    deliveryAddress = delivery.deliveryAddress
                )

                val deliveryResult = receiptPrinter.printDeliveryAuthorization(
                    sale = savedDeliveryAuthSale,
                    items = savedDeliveryAuthItems,
                    customer = customer,
                    businessInfo = businessInfo,
                    deliveryInfo = printerDeliveryInfo
                )

                if (deliveryResult.isSuccess) {
                    _message.value = "Printed: $receiptDocNumber & $deliveryDocNumber"
                } else {
                    _message.value = "Receipt printed, delivery auth failed: ${deliveryResult.exceptionOrNull()?.message}"
                }
            } else {
                _message.value = "Receipt printed: $receiptDocNumber"
            }

            clearCart()
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

class NewSaleViewModelFactory(
    private val customerDao: CustomerDao,
    private val itemDao: ItemDao,
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao,
    private val deliveryInfoDao: DeliveryInfoDao,
    private val appSettingsDao: AppSettingsDao,
    private val printerService: BluetoothPrinterService,
    private val receiptPrinter: ReceiptPrinter
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewSaleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewSaleViewModel(
                customerDao, itemDao, saleDao, saleItemDao,
                deliveryInfoDao, appSettingsDao, printerService, receiptPrinter
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
