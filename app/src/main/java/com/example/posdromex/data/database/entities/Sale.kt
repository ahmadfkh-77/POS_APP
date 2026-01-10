package com.example.posdromex.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long?,
    val type: String, // "RECEIPT" or "DELIVERY_AUTH"
    val documentNumber: String, // "R-000001" or "DA-000001"
    val date: Long, // timestamp
    val subtotal: Double,
    val tax: Double,
    val discount: Double,
    val total: Double,
    val status: String,
    val notes: String? = null,
    val receiptPrintCount: Int = 0,
    val deliveryAuthPrintCount: Int = 0
)

