package com.example.posdromex.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "delivery_info",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["saleId"], unique = true)]
)
data class DeliveryInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val driverName: String = "",
    val truckPlate: String = "",
    val emptyWeight: Double = 0.0,
    val fullWeight: Double = 0.0,
    val deliveryAddress: String = ""
) {
    val netWeight: Double get() = fullWeight - emptyWeight
}
