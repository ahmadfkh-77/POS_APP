package com.example.posdromex.ui.screens.settings

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posdromex.PosApplication

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            PosApplication.instance.database.appSettingsDao(),
            PosApplication.instance.printerService
        )
    )
) {
    val settings by viewModel.settings.collectAsState()
    val pairedDevices by viewModel.pairedDevices.collectAsState()
    val printerStatus by viewModel.printerStatus.collectAsState()
    val message by viewModel.message.collectAsState()

    var businessName by remember(settings) { mutableStateOf(settings?.businessName ?: "") }
    var businessPhone by remember(settings) { mutableStateOf(settings?.businessPhone ?: "") }
    var businessLocation by remember(settings) { mutableStateOf(settings?.businessLocation ?: "") }
    var receiptFooter by remember(settings) { mutableStateOf(settings?.receiptFooter ?: "") }

    // Bluetooth permissions
    val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            viewModel.loadPairedDevices()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(bluetoothPermissions)
    }

    // Snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Printer Section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Bluetooth Printer",
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = { viewModel.loadPairedDevices() }) {
                                Icon(Icons.Default.Refresh, "Refresh")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = when (printerStatus) {
                                    is SettingsViewModel.PrinterStatus.Connected -> MaterialTheme.colorScheme.primary
                                    is SettingsViewModel.PrinterStatus.Connecting -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            )
                            Text(
                                when (printerStatus) {
                                    is SettingsViewModel.PrinterStatus.Connected -> "Connected: ${settings?.printerName ?: "Unknown"}"
                                    is SettingsViewModel.PrinterStatus.Connecting -> "Connecting..."
                                    is SettingsViewModel.PrinterStatus.Disconnected -> "Not connected"
                                    is SettingsViewModel.PrinterStatus.Error -> "Error: ${(printerStatus as SettingsViewModel.PrinterStatus.Error).message}"
                                }
                            )
                        }

                        if (printerStatus is SettingsViewModel.PrinterStatus.Connected) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { viewModel.printTestPage() }) {
                                    Icon(Icons.Default.Print, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Test Print")
                                }
                                OutlinedButton(onClick = { viewModel.disconnectPrinter() }) {
                                    Text("Disconnect")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Paired Devices:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (pairedDevices.isEmpty()) {
                            Text(
                                "No paired devices found. Pair your printer in Android Bluetooth settings first.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Paired devices list
            items(pairedDevices) { device ->
                BluetoothDeviceItem(
                    device = device,
                    isSelected = device.address == settings?.printerMacAddress,
                    isConnected = printerStatus is SettingsViewModel.PrinterStatus.Connected &&
                            device.address == settings?.printerMacAddress,
                    onClick = { viewModel.connectToPrinter(device) }
                )
            }

            // Business Info Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Business Information",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Business Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = businessPhone,
                            onValueChange = { businessPhone = it },
                            label = { Text("Phone") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = businessLocation,
                            onValueChange = { businessLocation = it },
                            label = { Text("Location") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = receiptFooter,
                            onValueChange = { receiptFooter = it },
                            label = { Text("Receipt Footer") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.updateBusinessInfo(
                                    businessName, businessPhone, businessLocation, receiptFooter
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Business Info")
                        }
                    }
                }
            }

            // Document Numbering Info
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Document Numbering",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Next Receipt: ${settings?.receiptPrefix}${settings?.nextReceiptNumber?.toString()?.padStart(6, '0')}")
                        Text("Next Delivery Auth: ${settings?.deliveryAuthPrefix}${settings?.nextDeliveryAuthNumber?.toString()?.padStart(6, '0')}")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun BluetoothDeviceItem(
    device: BluetoothDevice,
    isSelected: Boolean,
    isConnected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    device.name ?: "Unknown Device",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isConnected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Connected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
