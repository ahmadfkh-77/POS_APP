package com.example.posdromex.ui.screens.conversions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posdromex.PosApplication
import com.example.posdromex.data.database.entities.ConversionRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConversionsViewModel = viewModel(
        factory = ConversionsViewModelFactory(PosApplication.instance.database)
    )
) {
    val conversions by viewModel.conversions.collectAsState(initial = emptyList())
    var showAddConversionDialog by remember { mutableStateOf(false) }
    var conversionToEdit by remember { mutableStateOf<ConversionRule?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit Conversions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddConversionDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Conversion")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How Conversions Work",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Example: Cement - 2400 kg = 1 m³",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "To convert kg to m³: DIVIDE by 2400",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (conversions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No conversions found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(conversions) { conversion ->
                        ConversionListItem(
                            conversion = conversion,
                            onEdit = { conversionToEdit = conversion },
                            onDelete = { viewModel.deleteConversion(conversion) }
                        )
                    }
                }
            }
        }
    }

    if (showAddConversionDialog) {
        ConversionDialog(
            title = "Add Conversion",
            initialConversion = null,
            onDismiss = { showAddConversionDialog = false },
            onConfirm = { name, fromUnit, toUnit, operation, factor, decimals ->
                viewModel.addConversion(name, fromUnit, toUnit, operation, factor, decimals)
                showAddConversionDialog = false
            }
        )
    }

    conversionToEdit?.let { conversion ->
        ConversionDialog(
            title = "Edit Conversion",
            initialConversion = conversion,
            onDismiss = { conversionToEdit = null },
            onConfirm = { name, fromUnit, toUnit, operation, factor, decimals ->
                viewModel.updateConversion(
                    conversion.copy(
                        name = name,
                        fromUnit = fromUnit,
                        toUnit = toUnit,
                        operation = operation,
                        factor = factor,
                        decimals = decimals
                    )
                )
                conversionToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversionDialog(
    title: String,
    initialConversion: ConversionRule?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, fromUnit: String, toUnit: String, operation: String, factor: Double, decimals: Int) -> Unit
) {
    var name by remember { mutableStateOf(initialConversion?.name ?: "") }
    var fromUnit by remember { mutableStateOf(initialConversion?.fromUnit ?: "kg") }
    var toUnit by remember { mutableStateOf(initialConversion?.toUnit ?: "m³") }
    var operation by remember { mutableStateOf(initialConversion?.operation ?: "DIVIDE") }
    var factor by remember { mutableStateOf(initialConversion?.factor?.toString() ?: "") }
    var decimals by remember { mutableStateOf(initialConversion?.decimals?.toString() ?: "2") }
    var operationDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    placeholder = { Text("e.g., Cement kg to m³") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fromUnit,
                        onValueChange = { fromUnit = it },
                        label = { Text("From Unit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = toUnit,
                        onValueChange = { toUnit = it },
                        label = { Text("To Unit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Operation dropdown
                ExposedDropdownMenuBox(
                    expanded = operationDropdownExpanded,
                    onExpandedChange = { operationDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (operation == "DIVIDE") "Divide" else "Multiply",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Operation") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = operationDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = operationDropdownExpanded,
                        onDismissRequest = { operationDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Divide") },
                            onClick = {
                                operation = "DIVIDE"
                                operationDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Multiply") },
                            onClick = {
                                operation = "MULTIPLY"
                                operationDropdownExpanded = false
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = factor,
                    onValueChange = { factor = it },
                    label = { Text("Factor *") },
                    placeholder = { Text("e.g., 2400") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedTextField(
                    value = decimals,
                    onValueChange = { decimals = it },
                    label = { Text("Decimal Places") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Preview calculation
                val factorValue = factor.toDoubleOrNull() ?: 0.0
                if (factorValue > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Preview:",
                                style = MaterialTheme.typography.labelSmall
                            )
                            val result = if (operation == "DIVIDE") {
                                1000 / factorValue
                            } else {
                                1000 * factorValue
                            }
                            Text(
                                text = "1000 $fromUnit = ${String.format("%.${decimals.toIntOrNull() ?: 2}f", result)} $toUnit",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val factorValue = factor.toDoubleOrNull() ?: 0.0
                    val decimalsValue = decimals.toIntOrNull() ?: 2
                    if (name.isNotBlank() && factorValue > 0) {
                        onConfirm(name, fromUnit, toUnit, operation, factorValue, decimalsValue)
                    }
                },
                enabled = name.isNotBlank() && (factor.toDoubleOrNull() ?: 0.0) > 0
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

@Composable
fun ConversionListItem(
    conversion: ConversionRule,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversion.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${conversion.fromUnit} → ${conversion.toUnit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${if (conversion.operation == "DIVIDE") "÷" else "×"} ${conversion.factor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
