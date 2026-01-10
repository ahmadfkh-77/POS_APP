package com.example.posdromex.ui.screens.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posdromex.data.database.entities.Sale
import com.example.posdromex.ui.screens.clients.viewmodel.ClientProfileViewModel
import com.example.posdromex.ui.screens.clients.viewmodel.ClientProfileViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(
    customerId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit = {},
    viewModel: ClientProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ClientProfileViewModelFactory(
            customerId = customerId,
            database = com.example.posdromex.PosApplication.instance.database
        )
    )
) {
    val customer by viewModel.customer.collectAsState()
    val receipts by viewModel.receipts.collectAsState(initial = emptyList())
    val deliveryAuths by viewModel.deliveryAuthorizations.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Client Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (customer == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    ClientInfoSection(customer = customer!!)
                }

                item {
                    Text(
                        text = "Receipts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (receipts.isEmpty()) {
                    item {
                        Text(
                            text = "No receipts found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(receipts) { receipt ->
                        SaleListItem(
                            sale = receipt,
                            onClick = { onNavigateToOrderDetail(receipt.id) }
                        )
                    }
                }

                item {
                    Text(
                        text = "Delivery Authorizations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (deliveryAuths.isEmpty()) {
                    item {
                        Text(
                            text = "No delivery authorizations found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(deliveryAuths) { auth ->
                        SaleListItem(
                            sale = auth,
                            onClick = { onNavigateToOrderDetail(auth.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClientInfoSection(customer: com.example.posdromex.data.database.entities.Customer) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Client Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            InfoRow(label = "Name", value = customer.name)
            customer.phone?.let {
                InfoRow(label = "Phone", value = it)
            }
            customer.address?.let {
                InfoRow(label = "Address", value = it)
            }
            customer.notes?.let {
                InfoRow(label = "Notes", value = it)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
fun SaleListItem(sale: Sale, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = sale.documentNumber,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatDate(sale.date),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Total: $${String.format("%.2f", sale.total)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Tap to view details",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

