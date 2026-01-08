package com.example.posdromex.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "units")
data class Unit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // kg, ton, m3, piece, bag, truck, etc.
    val symbol: String, // Display symbol
    val isActive: Boolean = true
)
