package com.example.posdromex.ui.screens.quickprint

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.posdromex.PosApplication
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTextPrintScreen(
    onNavigateBack: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val printerService = PosApplication.instance.printerService
    val receiptPrinter = PosApplication.instance.receiptPrinter

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Text Print") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp)
        ) {
            // Connection status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (printerService.isConnected())
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = if (printerService.isConnected())
                        "Printer Connected"
                    else
                        "Printer Not Connected - Go to Settings",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text input
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Text to print") },
                placeholder = { Text("Paste or type any text here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 10
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { text = "" },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Clear")
                }

                Button(
                    onClick = {
                        scope.launch {
                            if (!printerService.isConnected()) {
                                snackbarHostState.showSnackbar("Printer not connected! Go to Settings first.")
                                return@launch
                            }
                            if (text.isBlank()) {
                                snackbarHostState.showSnackbar("Nothing to print")
                                return@launch
                            }
                            val result = receiptPrinter.printPlainText(text)
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Printed successfully!")
                            } else {
                                snackbarHostState.showSnackbar("Print failed: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = printerService.isConnected() && text.isNotBlank()
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Print")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info text
            Text(
                "Paste any text (like messages from your iPhone) and print instantly on your 58mm thermal printer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
