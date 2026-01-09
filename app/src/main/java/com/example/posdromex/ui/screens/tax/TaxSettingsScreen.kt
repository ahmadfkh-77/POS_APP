package com.example.posdromex.ui.screens.tax

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posdromex.PosApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TaxSettingsViewModel = viewModel(
        factory = TaxSettingsViewModelFactory(PosApplication.instance.database.appSettingsDao())
    )
) {
    val settings by viewModel.settings.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var taxRate by remember { mutableStateOf("") }

    // Initialize tax rate when settings load
    LaunchedEffect(settings) {
        settings?.let {
            taxRate = if (it.defaultTaxRate > 0) it.defaultTaxRate.toString() else ""
        }
    }

    // Show messages
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tax Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Global Tax Rate",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "This tax rate will be applied to all items unless the item has its own tax rate set.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = taxRate,
                onValueChange = { taxRate = it },
                label = { Text("Tax Rate (%)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = {
                    Text("Enter 0 or leave empty for no tax")
                }
            )

            Button(
                onClick = {
                    val rate = taxRate.toDoubleOrNull() ?: 0.0
                    viewModel.updateTaxRate(rate)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Tax Rate")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "How Tax Works",
                style = MaterialTheme.typography.titleMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1. Global Tax Rate: Applied to all items by default",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "2. Item Tax Rate: If an item has its own tax rate set (> 0), it overrides the global rate",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "3. Tax Calculation: Tax = Subtotal x (Tax Rate / 100)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            settings?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Current Global Tax Rate",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "${it.defaultTaxRate}%",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }
    }
}
