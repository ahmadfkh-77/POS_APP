package com.example.posdromex.printer

import com.example.posdromex.data.database.entities.Sale
import com.example.posdromex.data.database.entities.SaleItem
import com.example.posdromex.data.database.entities.Customer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Receipt Printer Helper - Formats and prints receipts and delivery authorizations
 * Optimized for 58mm (32 char) ESC/POS thermal printers
 */
class ReceiptPrinter(private val printerService: BluetoothPrinterService) {

    data class BusinessInfo(
        val name: String = "My Business",
        val phone: String = "",
        val location: String = "",
        val footer: String = "Thank you!"
    )

    data class DeliveryInfo(
        val driverName: String = "",
        val truckPlate: String = "",
        val emptyWeight: Double = 0.0,
        val fullWeight: Double = 0.0,
        val deliveryAddress: String = ""
    )

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    /**
     * Print a receipt (WITH prices)
     */
    suspend fun printReceipt(
        sale: Sale,
        items: List<SaleItem>,
        customer: Customer?,
        businessInfo: BusinessInfo,
        currency: String = "USD",
        exchangeRate: Double? = null
    ): Result<Unit> {
        return try {
            // Header
            printerService.printRaw(BluetoothPrinterService.ESC_INIT)
            printerService.printText(businessInfo.name, bold = true, centered = true, doubleSize = true)
            if (businessInfo.phone.isNotBlank()) {
                printerService.printText(businessInfo.phone, centered = true)
            }
            if (businessInfo.location.isNotBlank()) {
                printerService.printText(businessInfo.location, centered = true)
            }
            printerService.printLine()

            // Document info
            printerService.printText("RECEIPT", bold = true, centered = true)
            printerService.printText("No: ${sale.documentNumber}", centered = true)
            printerService.printText(dateFormat.format(Date(sale.date)), centered = true)
            printerService.printLine()

            // Customer info
            if (customer != null) {
                printerService.printText("Customer: ${customer.name}")
                if (!customer.phone.isNullOrBlank()) {
                    printerService.printText("Phone: ${customer.phone}")
                }
            }
            printerService.printLine()

            // Items
            for (item in items) {
                // Item name
                printerService.printText(item.productName, bold = true)
                // Quantity and unit price
                val qtyStr = "${formatNumber(item.quantity)} ${item.unit}"
                val priceStr = "@ ${formatMoney(item.unitPrice)}"
                printerService.printTwoColumns(qtyStr, priceStr)
                // Conversion if applicable
                if (!item.conversionRuleName.isNullOrBlank()) {
                    printerService.printText("  (${item.conversionRuleName})")
                }
                // Line total
                printerService.printTwoColumns("", formatMoney(item.total))
            }
            printerService.printLine()

            // Totals
            printerService.printTwoColumns("Subtotal:", formatMoney(sale.subtotal))
            if (sale.discount > 0) {
                printerService.printTwoColumns("Discount:", "-${formatMoney(sale.discount)}")
            }
            if (sale.tax > 0) {
                printerService.printTwoColumns("Tax:", formatMoney(sale.tax))
            }
            printerService.printLine()
            printerService.printText("TOTAL: ${formatMoney(sale.total)} $currency", bold = true)

            if (exchangeRate != null && exchangeRate != 1.0) {
                printerService.printText("Rate: $exchangeRate")
            }

            // Payment info
            printerService.printLine()
            printerService.printText("Payment: ${sale.status}")

            // Footer
            printerService.printLine()
            printerService.printText(businessInfo.footer, centered = true)
            printerService.feedLines(3)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Print a Delivery Authorization (NO prices)
     */
    suspend fun printDeliveryAuthorization(
        sale: Sale,
        items: List<SaleItem>,
        customer: Customer?,
        businessInfo: BusinessInfo,
        deliveryInfo: DeliveryInfo
    ): Result<Unit> {
        return try {
            // Header
            printerService.printRaw(BluetoothPrinterService.ESC_INIT)
            printerService.printText(businessInfo.name, bold = true, centered = true, doubleSize = true)
            if (businessInfo.phone.isNotBlank()) {
                printerService.printText(businessInfo.phone, centered = true)
            }
            if (businessInfo.location.isNotBlank()) {
                printerService.printText(businessInfo.location, centered = true)
            }
            printerService.printLine()

            // Document info
            printerService.printText("DELIVERY AUTH", bold = true, centered = true)
            printerService.printText("No: ${sale.documentNumber}", centered = true)
            printerService.printText(dateFormat.format(Date(sale.date)), centered = true)
            printerService.printLine()

            // Customer info
            if (customer != null) {
                printerService.printText("Customer: ${customer.name}")
                if (!customer.phone.isNullOrBlank()) {
                    printerService.printText("Phone: ${customer.phone}")
                }
            }

            // Delivery address
            if (deliveryInfo.deliveryAddress.isNotBlank()) {
                printerService.printText("Deliver to:")
                printerService.printText(deliveryInfo.deliveryAddress)
            }
            printerService.printLine()

            // Materials list (NO PRICES)
            printerService.printText("MATERIALS:", bold = true)
            for (item in items) {
                val qtyStr = "${formatNumber(item.quantity)} ${item.unit}"
                printerService.printTwoColumns(item.productName, qtyStr)
                if (!item.conversionRuleName.isNullOrBlank()) {
                    printerService.printText("  (${item.conversionRuleName})")
                }
            }
            printerService.printLine()

            // Driver and truck info
            if (deliveryInfo.driverName.isNotBlank()) {
                printerService.printText("Driver: ${deliveryInfo.driverName}")
            }
            if (deliveryInfo.truckPlate.isNotBlank()) {
                printerService.printText("Truck: ${deliveryInfo.truckPlate}")
            }
            printerService.printLine()

            // Weights
            printerService.printText("WEIGHTS:", bold = true)
            printerService.printTwoColumns("Empty:", "${formatNumber(deliveryInfo.emptyWeight)} kg")
            printerService.printTwoColumns("Full:", "${formatNumber(deliveryInfo.fullWeight)} kg")
            val netWeight = deliveryInfo.fullWeight - deliveryInfo.emptyWeight
            printerService.printTwoColumns("Net:", "${formatNumber(netWeight)} kg")
            printerService.printLine()

            // Signature lines
            printerService.feedLines(2)
            printerService.printLine()
            printerService.printText("Driver Signature", centered = true)
            printerService.feedLines(2)
            printerService.printLine()
            printerService.printText("Receiver Signature", centered = true)
            printerService.feedLines(3)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Print plain text (for Quick Text Print feature)
     */
    suspend fun printPlainText(text: String): Result<Unit> {
        return try {
            printerService.printRaw(BluetoothPrinterService.ESC_INIT)

            // Split text into lines and print
            val lines = text.split("\n")
            for (line in lines) {
                // Wrap long lines
                if (line.length > BluetoothPrinterService.CHARS_PER_LINE) {
                    val chunks = line.chunked(BluetoothPrinterService.CHARS_PER_LINE)
                    for (chunk in chunks) {
                        printerService.printText(chunk)
                    }
                } else {
                    printerService.printText(line)
                }
            }

            printerService.feedLines(3)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatMoney(amount: Double): String {
        return String.format(Locale.US, "%.2f", amount)
    }

    private fun formatNumber(number: Double): String {
        return if (number == number.toLong().toDouble()) {
            number.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", number)
        }
    }
}
