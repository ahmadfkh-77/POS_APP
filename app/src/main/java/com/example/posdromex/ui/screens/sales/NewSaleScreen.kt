package com.example.posdromex.ui.screens.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posdromex.PosApplication
import com.example.posdromex.data.database.entities.Customer
import com.example.posdromex.ui.screens.sales.DeliveryInfoInput
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleScreen(
    onNavigateBack: () -> Unit,
    viewModel: NewSaleViewModel = viewModel(
        factory = NewSaleViewModelFactory(
            PosApplication.instance.database.customerDao(),
            PosApplication.instance.database.itemDao(),
            PosApplication.instance.database.saleDao(),
            PosApplication.instance.database.saleItemDao(),
            PosApplication.instance.database.deliveryInfoDao(),
            PosApplication.instance.database.appSettingsDao(),
            PosApplication.instance.printerService,
            PosApplication.instance.receiptPrinter
        )
    )
) {
    val customers by viewModel.customers.collectAsState()
    val items by viewModel.items.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val deliveryInfo by viewModel.deliveryInfo.collectAsState()
    val printBothDocuments by viewModel.printBothDocuments.collectAsState()
    val message by viewModel.message.collectAsState()

    var showCustomerDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showDeliveryInfoDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val printerConnected = PosApplication.instance.printerService.isConnected()

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Sale") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Combined print toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Print Receipt + Delivery Auth",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = printBothDocuments,
                            onCheckedChange = { viewModel.togglePrintBothDocuments() },
                            enabled = deliveryInfo.hasData()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL", style = MaterialTheme.typography.titleLarge)
                        Text(
                            String.format(Locale.US, "$%.2f", viewModel.subtotal),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Print button
                    Button(
                        onClick = { viewModel.printSale() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = printerConnected && cartItems.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            when {
                                !printerConnected -> "Printer Not Connected"
                                printBothDocuments && deliveryInfo.hasData() -> "Print Receipt + Delivery"
                                else -> "Print Receipt"
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Customer selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showCustomerDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                selectedCustomer?.name ?: "Select Customer (Optional)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            selectedCustomer?.phone?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Delivery info card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDeliveryInfoDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (deliveryInfo.hasData()) "Delivery Info" else "Add Delivery Info (Optional)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (deliveryInfo.hasData()) {
                                if (deliveryInfo.driverName.isNotBlank()) {
                                    Text("Driver: ${deliveryInfo.driverName}", style = MaterialTheme.typography.bodySmall)
                                }
                                if (deliveryInfo.truckPlate.isNotBlank()) {
                                    Text("Truck: ${deliveryInfo.truckPlate}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        if (deliveryInfo.hasData()) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Add item button
            item {
                OutlinedButton(
                    onClick = { showAddItemDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Item")
                }
            }

            // Cart items
            if (cartItems.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "No items added yet",
                            modifier = Modifier.padding(32.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            itemsIndexed(cartItems) { index, cartItem ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                cartItem.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "${cartItem.quantity} ${cartItem.unit} @ $${String.format(Locale.US, "%.2f", cartItem.unitPrice)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            String.format(Locale.US, "$%.2f", cartItem.total),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { viewModel.removeFromCart(index) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // Customer selection dialog
    if (showCustomerDialog) {
        CustomerSelectionDialog(
            customers = customers,
            onDismiss = { showCustomerDialog = false },
            onSelect = { customer ->
                viewModel.selectCustomer(customer)
                showCustomerDialog = false
            }
        )
    }

    // Add item dialog
    if (showAddItemDialog) {
        AddItemDialog(
            onDismiss = { showAddItemDialog = false },
            onAdd = { name, qty, unit, price ->
                viewModel.addManualItem(name, qty, unit, price)
                showAddItemDialog = false
            }
        )
    }

    // Delivery info dialog
    if (showDeliveryInfoDialog) {
        DeliveryInfoDialog(
            deliveryInfo = deliveryInfo,
            onDismiss = { showDeliveryInfoDialog = false },
            onSave = { info ->
                viewModel.updateDeliveryInfo(info)
                showDeliveryInfoDialog = false
            }
        )
    }
}

@Composable
private fun CustomerSelectionDialog(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onSelect: (Customer) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Customer") },
        text = {
            LazyColumn {
                if (customers.isEmpty()) {
                    item {
                        Text("No customers found. Add customers first.")
                    }
                }
                items(customers.size) { index ->
                    val customer = customers[index]
                    TextButton(
                        onClick = { onSelect(customer) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(customer.name, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, quantity: Double, unit: String, price: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("kg") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Qty") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Unit Price") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = quantity.toDoubleOrNull() ?: 1.0
                    val prc = price.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onAdd(name, qty, unit, prc)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DeliveryInfoDialog(
    deliveryInfo: DeliveryInfoInput,
    onDismiss: () -> Unit,
    onSave: (DeliveryInfoInput) -> Unit
) {
    var driverName by remember { mutableStateOf(deliveryInfo.driverName) }
    var truckPlate by remember { mutableStateOf(deliveryInfo.truckPlate) }
    var emptyWeight by remember { mutableStateOf(deliveryInfo.emptyWeight) }
    var fullWeight by remember { mutableStateOf(deliveryInfo.fullWeight) }
    var deliveryAddress by remember { mutableStateOf(deliveryInfo.deliveryAddress) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Delivery Information")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = driverName,
                    onValueChange = { driverName = it },
                    label = { Text("Driver Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = truckPlate,
                    onValueChange = { truckPlate = it },
                    label = { Text("Truck Plate") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = emptyWeight,
                    onValueChange = { emptyWeight = it },
                    label = { Text("Empty Weight") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = fullWeight,
                    onValueChange = { fullWeight = it },
                    label = { Text("Full Weight") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Delivery Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        DeliveryInfoInput(
                            driverName = driverName,
                            truckPlate = truckPlate,
                            emptyWeight = emptyWeight,
                            fullWeight = fullWeight,
                            deliveryAddress = deliveryAddress
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}