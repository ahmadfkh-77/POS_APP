package com.example.posdromex.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1, // Single row for app settings

    // Business Info
    val businessName: String = "My Business",
    val businessPhone: String = "",
    val businessLocation: String = "",
    val receiptFooter: String = "Thank you!",

    // Printer Settings
    val printerMacAddress: String = "",
    val printerName: String = "",

    // Document Numbering
    val receiptPrefix: String = "R-",
    val deliveryAuthPrefix: String = "DA-",
    val nextReceiptNumber: Int = 1,
    val nextDeliveryAuthNumber: Int = 1,
    val numberingResetMode: String = "NEVER", // NEVER, YEARLY, MONTHLY

    // Currency
    val defaultCurrency: String = "USD",
    val exchangeRate: Double = 1.0,

    // Backup Settings
    val autoBackupEnabled: Boolean = true,
    val backupRetentionDays: Int = 14,
    val lastBackupDate: Long = 0
)
