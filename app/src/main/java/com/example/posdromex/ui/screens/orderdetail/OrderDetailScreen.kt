package com.example.posdromex.ui.screens.orderdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posdromex.PosApplication
import com.example.posdromex.data.database.entities.DeliveryInfo
import com.example.posdromex.data.database.entities.Sale
import com.example.posdromex.data.database.entities.SaleItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    saleId: Long,
    onNavigateBack: () -> Unit,
    viewModel: OrderDetailViewModel = viewModel(
        factory = OrderDetailViewModelFactory(
            saleId = saleId,
            saleDao = PosApplication.instance.database.saleDao(),
            saleItemDao = PosApplication.instance.database.saleItemDao(),
            customerDao = PosApplication.instance.database.customerDao(),
            deliveryInfoDao = PosApplication.instance.database.deliveryInfoDao(),
            appSettingsDao = PosApplication.instance.database.appSettingsDao(),
            printerService = PosApplication.instance.printerService,
            receiptPrinter = PosApplication.instance.receiptPrinter
        )
    )
) {
    val sale by viewModel.sale.collectAsState()
    val saleItems by viewModel.saleItems.collectAsState()
    val customer by viewModel.customer.collectAsState()
    val deliveryInfo by viewModel.deliveryInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

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
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            sale?.let { currentSale ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Print status indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Receipt prints: ${currentSale.receiptPrintCount}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Delivery prints: ${currentSale.deliveryAuthPrintCount}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Print buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.printReceipt() },
                                modifier = Modifier.weight(1f),
                                enabled = printerConnected
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Print Receipt")
                                    Text(
                                        if (currentSale.receiptPrintCount == 0) "ORIGINAL" else "COPY",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.printDeliveryAuth() },
                                modifier = Modifier.weight(1f),
                                enabled = printerConnected && deliveryInfo != null
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Print Delivery")
                                    Text(
                                        if (currentSale.deliveryAuthPrintCount == 0) "ORIGINAL" else "COPY",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
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
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (sale == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Order not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Order header
                item {
                    OrderHeaderCard(sale = sale!!, customer = customer)
                }

                // Delivery info if exists
                deliveryInfo?.let { info ->
                    item {
                        DeliveryInfoCard(deliveryInfo = info)
                    }
                }

                // Items section header
                item {
                    Text(
                        "Items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Sale items
                items(saleItems) { item ->
                    SaleItemCard(item = item)
                }

                // Totals
                item {
                    TotalsCard(sale = sale!!)
                }

                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}

@Composable
private fun OrderHeaderCard(
    sale: Sale,
    customer: com.example.posdromex.data.database.entities.Customer?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    sale.documentNumber,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                AssistChip(
                    onClick = {},
                    label = { Text(sale.type.replace("_", " ")) }
                )
            }

            Text(
                formatDateTime(sale.date),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            customer?.let {
                Text("Customer: ${it.name}", style = MaterialTheme.typography.bodyMedium)
                it.phone?.let { phone ->
                    Text("Phone: $phone", style = MaterialTheme.typography.bodySmall)
                }
            } ?: Text(
                "No customer selected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeliveryInfoCard(deliveryInfo: DeliveryInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Delivery Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            if (deliveryInfo.driverName.isNotBlank()) {
                InfoRow(label = "Driver", value = deliveryInfo.driverName)
            }
            if (deliveryInfo.truckPlate.isNotBlank()) {
                InfoRow(label = "Truck", value = deliveryInfo.truckPlate)
            }
            if (deliveryInfo.deliveryAddress.isNotBlank()) {
                InfoRow(label = "Address", value = deliveryInfo.deliveryAddress)
            }

            HorizontalDivider()

            Text(
                "Weights",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            InfoRow(label = "Empty Weight", value = "${String.format(Locale.US, "%.2f", deliveryInfo.emptyWeight)} kg")
            InfoRow(label = "Full Weight", value = "${String.format(Locale.US, "%.2f", deliveryInfo.fullWeight)} kg")
            InfoRow(
                label = "Net Weight",
                value = "${String.format(Locale.US, "%.2f", deliveryInfo.netWeight)} kg",
                highlight = true
            )
        }
    }
}

@Composable
private fun SaleItemCard(item: SaleItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                // Show converted quantity if available, otherwise original
                if (item.convertedQuantity != null && item.convertedUnit != null) {
                    Text(
                        "${String.format(Locale.US, "%.2f", item.convertedQuantity)} ${item.convertedUnit} @ $${String.format(Locale.US, "%.2f", item.unitPrice)}/${item.convertedUnit}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "(Original: ${String.format(Locale.US, "%.2f", item.quantity)} ${item.unit})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "${String.format(Locale.US, "%.2f", item.quantity)} ${item.unit} @ $${String.format(Locale.US, "%.2f", item.unitPrice)}/${item.unit}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                item.conversionRuleName?.let {
                    Text(
                        "Conversion: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                String.format(Locale.US, "$%.2f", item.total),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TotalsCard(sale: Sale) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                Text(String.format(Locale.US, "$%.2f", sale.subtotal), style = MaterialTheme.typography.bodyMedium)
            }

            if (sale.discount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Discount", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "-${String.format(Locale.US, "$%.2f", sale.discount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (sale.tax > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tax", style = MaterialTheme.typography.bodyMedium)
                    Text(String.format(Locale.US, "$%.2f", sale.tax), style = MaterialTheme.typography.bodyMedium)
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TOTAL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    String.format(Locale.US, "$%.2f", sale.total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
