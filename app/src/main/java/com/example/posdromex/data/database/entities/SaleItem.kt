package com.example.posdromex.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val productName: String,
    val quantity: Double,
    val unit: String, // "kg", "ton", "m³", etc.
    val unitPrice: Double,
    val total: Double,
    val conversionRuleName: String? = null // nullable for optional conversions
)

