package com.example.posdromex.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onNavigateToClients: () -> Unit,
    onNavigateToNewSale: () -> Unit,
    onNavigateToQuickTextPrint: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDrivers: () -> Unit,
    onNavigateToTrucks: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToItems: () -> Unit,
    onNavigateToTaxSettings: () -> Unit,
    onNavigateToConversions: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("POSDromex") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Main Actions
            Text(
                text = "Sales",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onNavigateToNewSale,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New Sale", style = MaterialTheme.typography.titleMedium)
            }

            Button(
                onClick = onNavigateToClients,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Icon(Icons.Default.People, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Clients", style = MaterialTheme.typography.titleMedium)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Catalog Management
            Text(
                text = "Catalog",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToCategories,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Category, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Categories")
                }
                OutlinedButton(
                    onClick = onNavigateToItems,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Inventory, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Items")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Delivery Management
            Text(
                text = "Delivery",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToDrivers,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Drivers")
                }
                OutlinedButton(
                    onClick = onNavigateToTrucks,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Trucks")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Settings
            Text(
                text = "Configuration",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToTaxSettings,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Percent, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Tax")
                }
                OutlinedButton(
                    onClick = onNavigateToConversions,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Conversions")
                }
            }

            OutlinedButton(
                onClick = onNavigateToQuickTextPrint,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Quick Text Print")
            }

            Button(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Settings")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

