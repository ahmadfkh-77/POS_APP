package com.example.posdromex.ui.screens.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posdromex.PosApplication
import com.example.posdromex.data.database.entities.Category
import com.example.posdromex.data.database.entities.ConversionRule
import com.example.posdromex.data.database.entities.Customer
import com.example.posdromex.data.database.entities.Driver
import com.example.posdromex.data.database.entities.Item
import com.example.posdromex.data.database.entities.Truck
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleScreen(
    onNavigateBack: () -> Unit,
    viewModel: NewSaleViewModel = viewModel(
        factory = NewSaleViewModelFactory(
            PosApplication.instance.database.customerDao(),
            PosApplication.instance.database.itemDao(),
            PosApplication.instance.database.categoryDao(),
            PosApplication.instance.database.driverDao(),
            PosApplication.instance.database.truckDao(),
            PosApplication.instance.database.conversionRuleDao(),
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
    val categories by viewModel.categories.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    val trucks by viewModel.trucks.collectAsState()
    val conversionRules by viewModel.conversionRules.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val deliveryInfo by viewModel.deliveryInfo.collectAsState()
    val message by viewModel.message.collectAsState()

    var showCustomerDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showSelectItemDialog by remember { mutableStateOf(false) }
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
                    // Totals
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            String.format(Locale.US, "$%.2f", viewModel.subtotal),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (viewModel.taxAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tax", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                String.format(Locale.US, "$%.2f", viewModel.taxAmount),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL", style = MaterialTheme.typography.titleLarge)
                        Text(
                            String.format(Locale.US, "$%.2f", viewModel.total),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Two print buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Print Receipt button
                        Button(
                            onClick = { viewModel.printReceipt() },
                            modifier = Modifier.weight(1f),
                            enabled = printerConnected && cartItems.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Receipt")
                        }

                        // Print Delivery Auth button
                        Button(
                            onClick = { viewModel.printDeliveryAuth() },
                            modifier = Modifier.weight(1f),
                            enabled = printerConnected && cartItems.isNotEmpty() && deliveryInfo.hasData()
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Delivery")
                        }
                    }

                    if (!printerConnected) {
                        Text(
                            "Printer not connected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
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

            // Add item buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showSelectItemDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("From Catalog")
                    }
                    OutlinedButton(
                        onClick = { showAddItemDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Manual")
                    }
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
                            cartItem.convertedQuantity?.let { converted ->
                                Text(
                                    "→ ${String.format(Locale.US, "%.2f", converted)} ${cartItem.convertedUnit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (cartItem.taxRate > 0) {
                                Text(
                                    "Tax: ${cartItem.taxRate}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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

    // Select item from catalog dialog
    if (showSelectItemDialog) {
        SelectItemDialog(
            items = items,
            categories = categories,
            conversionRules = conversionRules,
            onDismiss = { showSelectItemDialog = false },
            onAdd = { item, qty, unit, conversion ->
                viewModel.addToCart(item, qty, unit, conversion)
                showSelectItemDialog = false
            }
        )
    }

    // Add manual item dialog
    if (showAddItemDialog) {
        AddItemDialog(
            conversionRules = conversionRules,
            onDismiss = { showAddItemDialog = false },
            onAdd = { name, qty, unit, price, conversion ->
                viewModel.addManualItem(name, qty, unit, price, conversion)
                showAddItemDialog = false
            }
        )
    }

    // Delivery info dialog
    if (showDeliveryInfoDialog) {
        DeliveryInfoDialog(
            deliveryInfo = deliveryInfo,
            drivers = drivers,
            trucks = trucks,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectItemDialog(
    items: List<Item>,
    categories: List<Category>,
    conversionRules: List<ConversionRule>,
    onDismiss: () -> Unit,
    onAdd: (item: Item, quantity: Double, unit: String, conversion: ConversionRule?) -> Unit
) {
    var selectedItem by remember { mutableStateOf<Item?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("") }
    var selectedConversion by remember { mutableStateOf<ConversionRule?>(null) }
    var conversionDropdownExpanded by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    val filteredItems = if (selectedCategoryId != null) {
        items.filter { it.categoryId == selectedCategoryId }
    } else {
        items
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Item from Catalog") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedItem == null) {
                    // Category filter
                    if (categories.isNotEmpty()) {
                        Text("Filter by category:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = selectedCategoryId == null,
                                onClick = { selectedCategoryId = null },
                                label = { Text("All") }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            categories.take(3).forEach { category ->
                                FilterChip(
                                    selected = selectedCategoryId == category.id,
                                    onClick = { selectedCategoryId = category.id },
                                    label = { Text(category.name, maxLines = 1) }
                                )
                            }
                        }
                    }

                    // Item list
                    if (filteredItems.isEmpty()) {
                        Text("No items found. Add items in catalog first.")
                    } else {
                        filteredItems.forEach { item ->
                            Card(
                                onClick = {
                                    selectedItem = item
                                    unit = item.defaultUnit
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.name, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "${item.defaultUnit} - $${item.price}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Item selected - show quantity input
                    Text("Selected: ${selectedItem!!.name}", style = MaterialTheme.typography.titleMedium)
                    Text("Price: $${selectedItem!!.price} / ${selectedItem!!.defaultUnit}")

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Conversion dropdown
                    if (conversionRules.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = conversionDropdownExpanded,
                            onExpandedChange = { conversionDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedConversion?.name ?: "No Conversion",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Convert to") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = conversionDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = conversionDropdownExpanded,
                                onDismissRequest = { conversionDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("No Conversion") },
                                    onClick = {
                                        selectedConversion = null
                                        conversionDropdownExpanded = false
                                    }
                                )
                                conversionRules.forEach { rule ->
                                    DropdownMenuItem(
                                        text = { Text(rule.name) },
                                        onClick = {
                                            selectedConversion = rule
                                            conversionDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    TextButton(onClick = { selectedItem = null }) {
                        Text("← Back to list")
                    }
                }
            }
        },
        confirmButton = {
            if (selectedItem != null) {
                TextButton(
                    onClick = {
                        val qty = quantity.toDoubleOrNull() ?: 1.0
                        selectedItem?.let { onAdd(it, qty, unit, selectedConversion) }
                    }
                ) {
                    Text("Add to Cart")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemDialog(
    conversionRules: List<ConversionRule>,
    onDismiss: () -> Unit,
    onAdd: (name: String, quantity: Double, unit: String, price: Double, conversion: ConversionRule?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("kg") }
    var price by remember { mutableStateOf("") }
    var selectedConversion by remember { mutableStateOf<ConversionRule?>(null) }
    var conversionDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Manual Item") },
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
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Conversion dropdown
                if (conversionRules.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = conversionDropdownExpanded,
                        onExpandedChange = { conversionDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedConversion?.name ?: "No Conversion",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Convert to") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = conversionDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = conversionDropdownExpanded,
                            onDismissRequest = { conversionDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("No Conversion") },
                                onClick = {
                                    selectedConversion = null
                                    conversionDropdownExpanded = false
                                }
                            )
                            conversionRules.forEach { rule ->
                                DropdownMenuItem(
                                    text = { Text(rule.name) },
                                    onClick = {
                                        selectedConversion = rule
                                        conversionDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = quantity.toDoubleOrNull() ?: 1.0
                    val prc = price.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onAdd(name, qty, unit, prc, selectedConversion)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeliveryInfoDialog(
    deliveryInfo: DeliveryInfoInput,
    drivers: List<Driver>,
    trucks: List<Truck>,
    onDismiss: () -> Unit,
    onSave: (DeliveryInfoInput) -> Unit
) {
    var selectedDriverId by remember { mutableStateOf(deliveryInfo.driverId) }
    var selectedDriverName by remember { mutableStateOf(deliveryInfo.driverName) }
    var selectedTruckId by remember { mutableStateOf(deliveryInfo.truckId) }
    var selectedTruckPlate by remember { mutableStateOf(deliveryInfo.truckPlate) }
    var emptyWeight by remember { mutableStateOf(deliveryInfo.emptyWeight) }
    var fullWeight by remember { mutableStateOf(deliveryInfo.fullWeight) }
    var deliveryAddress by remember { mutableStateOf(deliveryInfo.deliveryAddress) }

    var driverDropdownExpanded by remember { mutableStateOf(false) }
    var truckDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delivery Information") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Driver dropdown
                ExposedDropdownMenuBox(
                    expanded = driverDropdownExpanded,
                    onExpandedChange = { driverDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedDriverName.ifBlank { "Select Driver" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Driver") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = driverDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = driverDropdownExpanded,
                        onDismissRequest = { driverDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                selectedDriverId = null
                                selectedDriverName = ""
                                driverDropdownExpanded = false
                            }
                        )
                        drivers.forEach { driver ->
                            DropdownMenuItem(
                                text = { Text(driver.name) },
                                onClick = {
                                    selectedDriverId = driver.id
                                    selectedDriverName = driver.name
                                    driverDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (drivers.isEmpty()) {
                    Text(
                        "No drivers saved. Add drivers in main menu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Truck dropdown
                ExposedDropdownMenuBox(
                    expanded = truckDropdownExpanded,
                    onExpandedChange = { truckDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedTruckPlate.ifBlank { "Select Truck" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Truck Plate") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = truckDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = truckDropdownExpanded,
                        onDismissRequest = { truckDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                selectedTruckId = null
                                selectedTruckPlate = ""
                                truckDropdownExpanded = false
                            }
                        )
                        trucks.forEach { truck ->
                            DropdownMenuItem(
                                text = { Text(truck.plateNumber) },
                                onClick = {
                                    selectedTruckId = truck.id
                                    selectedTruckPlate = truck.plateNumber
                                    truckDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (trucks.isEmpty()) {
                    Text(
                        "No trucks saved. Add trucks in main menu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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
                            driverId = selectedDriverId,
                            driverName = selectedDriverName,
                            truckId = selectedTruckId,
                            truckPlate = selectedTruckPlate,
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
