package com.example.posdromex.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
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
    onNavigateToSettings: () -> Unit
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToNewSale,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New Sale", style = MaterialTheme.typography.titleLarge)
            }

            Button(
                onClick = onNavigateToClients,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Icon(Icons.Default.People, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Clients", style = MaterialTheme.typography.titleLarge)
            }

            Button(
                onClick = onNavigateToQuickTextPrint,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Quick Text Print", style = MaterialTheme.typography.titleLarge)
            }

            Button(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Settings", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

