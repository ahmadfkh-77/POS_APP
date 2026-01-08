package com.example.posdromex.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.posdromex.data.database.dao.AppSettingsDao
import com.example.posdromex.data.database.dao.CategoryDao
import com.example.posdromex.data.database.dao.ConversionRuleDao
import com.example.posdromex.data.database.dao.CustomerDao
import com.example.posdromex.data.database.dao.ItemDao
import com.example.posdromex.data.database.dao.SaleDao
import com.example.posdromex.data.database.dao.SaleItemDao
import com.example.posdromex.data.database.dao.UnitDao
import com.example.posdromex.data.database.entities.AppSettings
import com.example.posdromex.data.database.entities.Category
import com.example.posdromex.data.database.entities.ConversionRule
import com.example.posdromex.data.database.entities.Customer
import com.example.posdromex.data.database.entities.Item
import com.example.posdromex.data.database.entities.Sale
import com.example.posdromex.data.database.entities.SaleItem
import com.example.posdromex.data.database.entities.Unit

@Database(
    entities = [
        Customer::class,
        Sale::class,
        SaleItem::class,
        Category::class,
        Item::class,
        Unit::class,
        ConversionRule::class,
        AppSettings::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PosDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun categoryDao(): CategoryDao
    abstract fun itemDao(): ItemDao
    abstract fun unitDao(): UnitDao
    abstract fun conversionRuleDao(): ConversionRuleDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        const val DATABASE_NAME = "pos_database"
    }
}

