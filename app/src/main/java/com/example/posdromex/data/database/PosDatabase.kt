package com.example.posdromex.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.posdromex.data.database.dao.CustomerDao
import com.example.posdromex.data.database.dao.SaleDao
import com.example.posdromex.data.database.dao.SaleItemDao
import com.example.posdromex.data.database.entities.Customer
import com.example.posdromex.data.database.entities.Sale
import com.example.posdromex.data.database.entities.SaleItem

@Database(
    entities = [Customer::class, Sale::class, SaleItem::class],
    version = 1,
    exportSchema = false
)
abstract class PosDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao

    companion object {
        const val DATABASE_NAME = "pos_database"
    }
}

