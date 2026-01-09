package com.example.posdromex.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.posdromex.data.database.dao.AppSettingsDao
import com.example.posdromex.data.database.dao.CategoryDao
import com.example.posdromex.data.database.dao.ConversionRuleDao
import com.example.posdromex.data.database.dao.CustomerDao
import com.example.posdromex.data.database.dao.DeliveryInfoDao
import com.example.posdromex.data.database.dao.DriverDao
import com.example.posdromex.data.database.dao.ItemDao
import com.example.posdromex.data.database.dao.SaleDao
import com.example.posdromex.data.database.dao.SaleItemDao
import com.example.posdromex.data.database.dao.TruckDao
import com.example.posdromex.data.database.dao.UnitDao
import com.example.posdromex.data.database.entities.AppSettings
import com.example.posdromex.data.database.entities.Category
import com.example.posdromex.data.database.entities.ConversionRule
import com.example.posdromex.data.database.entities.Customer
import com.example.posdromex.data.database.entities.DeliveryInfo
import com.example.posdromex.data.database.entities.Driver
import com.example.posdromex.data.database.entities.Item
import com.example.posdromex.data.database.entities.Sale
import com.example.posdromex.data.database.entities.SaleItem
import com.example.posdromex.data.database.entities.Truck
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
        AppSettings::class,
        DeliveryInfo::class,
        Driver::class,
        Truck::class
    ],
    version = 5,
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
    abstract fun deliveryInfoDao(): DeliveryInfoDao
    abstract fun driverDao(): DriverDao
    abstract fun truckDao(): TruckDao

    companion object {
        const val DATABASE_NAME = "pos_database"

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create delivery_info table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS delivery_info (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        saleId INTEGER NOT NULL,
                        driverName TEXT NOT NULL DEFAULT '',
                        truckPlate TEXT NOT NULL DEFAULT '',
                        emptyWeight REAL NOT NULL DEFAULT 0.0,
                        fullWeight REAL NOT NULL DEFAULT 0.0,
                        deliveryAddress TEXT NOT NULL DEFAULT '',
                        FOREIGN KEY(saleId) REFERENCES sales(id) ON DELETE CASCADE
                    )
                """)

                // Create unique index for saleId
                db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_delivery_info_saleId
                    ON delivery_info(saleId)
                """)

                // Add new columns to app_settings
                db.execSQL("ALTER TABLE app_settings ADD COLUMN googleAccountEmail TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE app_settings ADD COLUMN backupFrequencyHours INTEGER NOT NULL DEFAULT 24")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create drivers table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS drivers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1
                    )
                """)

                // Create trucks table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS trucks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        plateNumber TEXT NOT NULL,
                        description TEXT DEFAULT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1
                    )
                """)

                // Add defaultTaxRate column to app_settings
                db.execSQL("ALTER TABLE app_settings ADD COLUMN defaultTaxRate REAL NOT NULL DEFAULT 0.0")
            }
        }
    }
}

