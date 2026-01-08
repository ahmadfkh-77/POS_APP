package com.example.posdromex.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onNavigateToClients: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("POS System") }
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
                onClick = onNavigateToClients,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Text("Clients", style = MaterialTheme.typography.titleLarge)
            }

            Button(
                onClick = { /* TODO: Navigate to Sales */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = false
            ) {
                Text("Sales", style = MaterialTheme.typography.titleLarge)
            }

            Button(
                onClick = { /* TODO: Navigate to Quick Text Print */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = false
            ) {
                Text("Quick Text Print", style = MaterialTheme.typography.titleLarge)
            }

            Button(
                onClick = { /* TODO: Navigate to Settings */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = false
            ) {
                Text("Settings", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

