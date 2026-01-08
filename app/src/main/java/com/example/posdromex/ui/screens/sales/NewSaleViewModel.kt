package com.example.posdromex.ui.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.posdromex.data.database.dao.AppSettingsDao
import com.example.posdromex.data.database.dao.CustomerDao
import com.example.posdromex.data.database.dao.ItemDao
import com.example.posdromex.data.database.dao.SaleDao
import com.example.posdromex.data.database.dao.SaleItemDao
import com.example.posdromex.data.database.entities.AppSettings
import com.example.posdromex.data.database.entities.Customer
import com.example.posdromex.data.database.entities.Item
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

data class CartItem(
    val name: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val conversionRule: String? = null
) {
    val total: Double get() = quantity * unitPrice
}

class NewSaleViewModel(
    private val customerDao: CustomerDao,
    private val itemDao: ItemDao,
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao,
    private val appSettingsDao: AppSettingsDao,
    private val printerService: BluetoothPrinterService,
    private val receiptPrinter: ReceiptPrinter
) : ViewModel() {

    val customers: StateFlow<List<Customer>> = customerDao.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val items: StateFlow<List<Item>> = itemDao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings?> = appSettingsDao.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val subtotal: Double
        get() = _cartItems.value.sumOf { it.total }

    fun selectCustomer(customer: Customer) {
        _selectedCustomer.value = customer
    }

    fun addToCart(item: Item, quantity: Double, unit: String) {
        val cartItem = CartItem(
            name = item.name,
            quantity = quantity,
            unit = unit,
            unitPrice = item.price
        )
        _cartItems.value = _cartItems.value + cartItem
    }

    fun addManualItem(name: String, quantity: Double, unit: String, price: Double) {
        val cartItem = CartItem(
            name = name,
            quantity = quantity,
            unit = unit,
            unitPrice = price
        )
        _cartItems.value = _cartItems.value + cartItem
    }

    fun removeFromCart(index: Int) {
        _cartItems.value = _cartItems.value.toMutableList().apply { removeAt(index) }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedCustomer.value = null
    }

    fun printReceipt() {
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

            // Generate document number
            val docNumber = "${currentSettings.receiptPrefix}${currentSettings.nextReceiptNumber.toString().padStart(6, '0')}"

            // Create sale record
            val sale = Sale(
                customerId = customer?.id ?: 0,
                type = "RECEIPT",
                documentNumber = docNumber,
                date = System.currentTimeMillis(),
                subtotal = subtotal,
                tax = 0.0,
                discount = 0.0,
                total = subtotal,
                status = "CASH"
            )

            val saleId = saleDao.insert(sale)

            // Create sale items
            val saleItems = cartList.map { cartItem ->
                SaleItem(
                    saleId = saleId,
                    productName = cartItem.name,
                    quantity = cartItem.quantity,
                    unit = cartItem.unit,
                    unitPrice = cartItem.unitPrice,
                    total = cartItem.total,
                    conversionRuleName = cartItem.conversionRule
                )
            }
            saleItemDao.insertItems(saleItems)

            // Increment receipt number
            appSettingsDao.incrementReceiptNumber()

            // Print receipt
            val savedSale = saleDao.getSaleById(saleId) ?: sale.copy(id = saleId)
            val savedItems = saleItemDao.getItemsBySaleIdSync(saleId)

            val businessInfo = ReceiptPrinter.BusinessInfo(
                name = currentSettings.businessName,
                phone = currentSettings.businessPhone,
                location = currentSettings.businessLocation,
                footer = currentSettings.receiptFooter
            )

            val result = receiptPrinter.printReceipt(
                sale = savedSale,
                items = savedItems,
                customer = customer,
                businessInfo = businessInfo,
                currency = currentSettings.defaultCurrency
            )

            if (result.isSuccess) {
                _message.value = "Receipt printed: $docNumber"
                clearCart()
            } else {
                _message.value = "Print failed: ${result.exceptionOrNull()?.message}"
            }
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
    private val appSettingsDao: AppSettingsDao,
    private val printerService: BluetoothPrinterService,
    private val receiptPrinter: ReceiptPrinter
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewSaleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewSaleViewModel(
                customerDao, itemDao, saleDao, saleItemDao,
                appSettingsDao, printerService, receiptPrinter
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
