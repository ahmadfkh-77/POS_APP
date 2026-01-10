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
     * @param isReprint true if this is a reprint (will show COPY instead of ORIGINAL)
     */
    suspend fun printReceipt(
        sale: Sale,
        items: List<SaleItem>,
        customer: Customer?,
        businessInfo: BusinessInfo,
        currency: String = "USD",
        exchangeRate: Double? = null,
        isReprint: Boolean = false
    ): Result<Unit> {
        return try {
            // Initialize printer
            printerService.printRaw(BluetoothPrinterService.ESC_INIT)

            // Header with business info
            printerService.printText("================================", centered = true)
            printerService.printText(processArabicText(businessInfo.name), bold = true, centered = true, doubleSize = true)
            if (businessInfo.phone.isNotBlank()) {
                printerService.printText(businessInfo.phone, centered = true)
            }
            if (businessInfo.location.isNotBlank()) {
                printerService.printText(processArabicText(businessInfo.location), centered = true)
            }
            printerService.printText("================================", centered = true)

            // ORIGINAL or COPY marking
            val printStatus = if (isReprint) "*** COPY / REPRINT ***" else "*** ORIGINAL ***"
            printerService.printText(printStatus, bold = true, centered = true)
            printerService.printText("")

            // Document info
            printerService.printText("RECEIPT", bold = true, centered = true)
            printerService.printText("No: ${sale.documentNumber}", centered = true)
            printerService.printText(dateFormat.format(Date(sale.date)), centered = true)
            printerService.printText("================================", centered = true)

            // Customer info
            if (customer != null) {
                printerService.printText("Customer: ${processArabicText(customer.name)}")
                if (!customer.phone.isNullOrBlank()) {
                    printerService.printText("Phone: ${customer.phone}")
                }
            } else {
                printerService.printText("Customer: Walk-in")
            }
            printerService.printText("--------------------------------", centered = true)

            // Items - show converted quantity if available
            for (item in items) {
                // Item name
                printerService.printText(processArabicText(item.productName), bold = true)

                // Show converted quantity and unit if conversion was used
                if (item.convertedQuantity != null && item.convertedUnit != null) {
                    // Price is per converted unit - normalize unit for printer
                    val displayUnit = normalizeUnit(item.convertedUnit)
                    val qtyStr = "${formatNumber(item.convertedQuantity)} $displayUnit"
                    // val priceStr = "$${formatMoney(item.unitPrice)}/$displayUnit"
                    printerService.printTwoColumns(qtyStr, priceStr)
                } else {
                    // No conversion - show original quantity with normalized unit
                    val displayUnit = normalizeUnit(item.unit)
                    val qtyStr = "${formatNumber(item.quantity)} $displayUnit"
                    val priceStr = "$${formatMoney(item.unitPrice)}/$displayUnit"
                    printerService.printTwoColumns(qtyStr, priceStr)
                }

                // Line total
                printerService.printTwoColumns("Line Total:", "$${formatMoney(item.total)}")
            }
            printerService.printText("================================", centered = true)

            // Totals
            printerService.printTwoColumns("Subtotal:", "$${formatMoney(sale.subtotal)}")
            if (sale.discount > 0) {
                printerService.printTwoColumns("Discount:", "-$${formatMoney(sale.discount)}")
            }
            if (sale.tax > 0) {
                // Calculate tax percentage from subtotal
                val taxPercent = if (sale.subtotal > 0) (sale.tax / sale.subtotal * 100).toInt() else 0
                printerService.printTwoColumns("Tax ($taxPercent%):", "$${formatMoney(sale.tax)}")
            }
            printerService.printText("--------------------------------", centered = true)
            printerService.printText("TOTAL: $${formatMoney(sale.total)} $currency", bold = true, centered = true)

            if (exchangeRate != null && exchangeRate != 1.0) {
                printerService.printText("Exchange Rate: $exchangeRate", centered = true)
            }

            printerService.printText("================================", centered = true)

            // Payment status
            printerService.printText("Payment: ${sale.status}", centered = true)

            // Footer
            printerService.printText("")
            printerService.printText(processArabicText(businessInfo.footer), centered = true)
            printerService.feedLines(3)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Print a Delivery Authorization (NO prices)
     * @param isReprint true if this is a reprint (will show COPY instead of ORIGINAL)
     */
    suspend fun printDeliveryAuthorization(
        sale: Sale,
        items: List<SaleItem>,
        customer: Customer?,
        businessInfo: BusinessInfo,
        deliveryInfo: DeliveryInfo,
        isReprint: Boolean = false
    ): Result<Unit> {
        return try {
            // Initialize printer
            printerService.printRaw(BluetoothPrinterService.ESC_INIT)

            // Header with business info
            printerService.printText("================================", centered = true)
            printerService.printText(processArabicText(businessInfo.name), bold = true, centered = true, doubleSize = true)
            if (businessInfo.phone.isNotBlank()) {
                printerService.printText(businessInfo.phone, centered = true)
            }
            if (businessInfo.location.isNotBlank()) {
                printerService.printText(processArabicText(businessInfo.location), centered = true)
            }
            printerService.printText("================================", centered = true)

            // ORIGINAL or COPY marking
            val printStatus = if (isReprint) "*** COPY / REPRINT ***" else "*** ORIGINAL ***"
            printerService.printText(printStatus, bold = true, centered = true)
            printerService.printText("")

            // Document info
            printerService.printText("DELIVERY AUTHORIZATION", bold = true, centered = true)
            printerService.printText("No: ${sale.documentNumber}", centered = true)
            printerService.printText(dateFormat.format(Date(sale.date)), centered = true)
            printerService.printText("================================", centered = true)

            // Customer info
            if (customer != null) {
                printerService.printText("Customer: ${processArabicText(customer.name)}")
                if (!customer.phone.isNullOrBlank()) {
                    printerService.printText("Phone: ${customer.phone}")
                }
            }

            // Delivery address
            if (deliveryInfo.deliveryAddress.isNotBlank()) {
                printerService.printText("Deliver to:")
                printerService.printText(processArabicText(deliveryInfo.deliveryAddress))
            }
            printerService.printText("--------------------------------", centered = true)

            // Materials list (NO PRICES) - show original quantity in kg first, then converted
            printerService.printText("MATERIALS:", bold = true)
            for (item in items) {
                printerService.printText(processArabicText(item.productName), bold = true)
                // Original quantity is always in kg (from net weight)
                printerService.printTwoColumns("  Quantity:", "${formatNumber(item.quantity)} kg")
                // Show converted quantity if conversion was applied
                if (item.convertedQuantity != null && item.convertedUnit != null) {
                    // Normalize unit display - replace problematic characters
                    val displayUnit = normalizeUnit(item.convertedUnit)
                    printerService.printTwoColumns("  Converted:", "${formatNumber(item.convertedQuantity)} $displayUnit")
                }
            }
            printerService.printText("--------------------------------", centered = true)

            // Driver and truck info
            printerService.printText("TRANSPORT:", bold = true)
            if (deliveryInfo.driverName.isNotBlank()) {
                printerService.printTwoColumns("Driver:", processArabicText(deliveryInfo.driverName))
            }
            if (deliveryInfo.truckPlate.isNotBlank()) {
                printerService.printTwoColumns("Truck:", deliveryInfo.truckPlate)
            }
            printerService.printText("--------------------------------", centered = true)

            // Weights
            printerService.printText("WEIGHTS:", bold = true)
            printerService.printTwoColumns("Empty:", "${formatNumber(deliveryInfo.emptyWeight)} kg")
            printerService.printTwoColumns("Full:", "${formatNumber(deliveryInfo.fullWeight)} kg")
            val netWeight = deliveryInfo.fullWeight - deliveryInfo.emptyWeight
            printerService.printText("--------------------------------", centered = true)
            printerService.printTwoColumns("NET WEIGHT:", "${formatNumber(netWeight)} kg")
            printerService.printText("================================", centered = true)

            // Signature lines
            printerService.printText("")
            printerService.printText("Driver Signature:", centered = true)
            printerService.feedLines(2)
            printerService.printText("________________________________", centered = true)
            printerService.printText("")
            printerService.printText("Receiver Signature:", centered = true)
            printerService.feedLines(2)
            printerService.printText("________________________________", centered = true)
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

    /**
     * Normalize unit strings for thermal printer output
     * Replaces problematic Unicode characters that may print incorrectly
     * (e.g., superscript characters, Chinese variants)
     */
    private fun normalizeUnit(unit: String): String {
        return unit
            // Replace various forms of cubic meter with simple "m3"
            .replace("\u33A5", "m3")      // ㎥ - Square Mq symbol
            .replace("\u7ACB\u65B9\u7C73", "m3")  // 立方米 - Chinese "cubic meter"
            .replace("m\u00B3", "m3")     // m³ - m with superscript 3
            .replace("M\u00B3", "m3")     // M³ - M with superscript 3
            .replace("\u00B3", "3")       // ³ - just superscript 3
            .replace("㎥", "m3")          // Another cubic meter symbol
            // Replace various forms of square meter
            .replace("\u33A1", "m2")      // ㎡ - Square meter symbol
            .replace("m\u00B2", "m2")     // m² - m with superscript 2
            .replace("\u00B2", "2")       // ² - just superscript 2
    }

    /**
     * Process Arabic text for thermal printer output
     * Arabic text needs to be reversed for proper RTL display on ESC/POS printers
     * Numbers within Arabic text should remain LTR
     */
    private fun processArabicText(text: String): String {
        // Check if text contains Arabic characters
        if (!containsArabic(text)) {
            return text
        }

        // Process the text for RTL display
        val result = StringBuilder()
        var currentSegment = StringBuilder()
        var isCurrentSegmentArabic = false

        for (char in text) {
            val isArabicChar = isArabicCharacter(char)
            val isNumber = char.isDigit()

            if (isNumber || char == '.' || char == ' ' || char == '-' || char == ':') {
                // Numbers, punctuation, and spaces - keep as is within the segment
                currentSegment.append(char)
            } else if (isArabicChar) {
                if (!isCurrentSegmentArabic && currentSegment.isNotEmpty()) {
                    // Switching from non-Arabic to Arabic - flush LTR segment
                    result.append(currentSegment)
                    currentSegment = StringBuilder()
                }
                isCurrentSegmentArabic = true
                currentSegment.append(char)
            } else {
                if (isCurrentSegmentArabic && currentSegment.isNotEmpty()) {
                    // Switching from Arabic to non-Arabic - reverse and flush Arabic segment
                    result.append(currentSegment.reverse())
                    currentSegment = StringBuilder()
                }
                isCurrentSegmentArabic = false
                currentSegment.append(char)
            }
        }

        // Flush remaining segment
        if (currentSegment.isNotEmpty()) {
            if (isCurrentSegmentArabic) {
                result.append(currentSegment.reverse())
            } else {
                result.append(currentSegment)
            }
        }

        // If the entire text was Arabic, reverse the whole result for proper display
        return if (containsArabic(text) && !text.any { !isArabicCharacter(it) && !it.isDigit() && it != ' ' && it != '.' && it != '-' && it != ':' }) {
            result.reverse().toString()
        } else {
            result.toString()
        }
    }

    /**
     * Check if text contains Arabic characters
     */
    private fun containsArabic(text: String): Boolean {
        return text.any { isArabicCharacter(it) }
    }

    /**
     * Check if a character is an Arabic character
     * Arabic Unicode range: U+0600 to U+06FF (Arabic)
     * Also includes: U+0750 to U+077F (Arabic Supplement)
     * Also includes: U+FB50 to U+FDFF (Arabic Presentation Forms-A)
     * Also includes: U+FE70 to U+FEFF (Arabic Presentation Forms-B)
     */
    private fun isArabicCharacter(char: Char): Boolean {
        val code = char.code
        return (code in 0x0600..0x06FF) ||
                (code in 0x0750..0x077F) ||
                (code in 0xFB50..0xFDFF) ||
                (code in 0xFE70..0xFEFF)
    }
}
