package com.example.posdromex.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversion_rules")
data class ConversionRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // User-defined name like "Concrete kg to m3"
    val fromUnit: String, // Source unit
    val toUnit: String, // Target unit
    val operation: String, // "DIVIDE" or "MULTIPLY"
    val factor: Double, // Conversion factor
    val decimals: Int = 2, // Number of decimal places for result
    val isActive: Boolean = true
)
