package com.example.posdromex

import android.app.Application
import androidx.room.Room
import com.example.posdromex.data.database.PosDatabase
import com.example.posdromex.printer.BluetoothPrinterService
import com.example.posdromex.printer.ReceiptPrinter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PosApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: PosDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            PosDatabase::class.java,
            PosDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For development - clears DB on schema change
            .build()
    }

    val printerService: BluetoothPrinterService by lazy {
        BluetoothPrinterService(applicationContext)
    }

    val receiptPrinter: ReceiptPrinter by lazy {
        ReceiptPrinter(printerService)
    }

    companion object {
        lateinit var instance: PosApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize default settings if not exist
        applicationScope.launch {
            initializeDefaultData()
        }
    }

    private suspend fun initializeDefaultData() {
        val settingsDao = database.appSettingsDao()
        if (settingsDao.getSettingsSync() == null) {
            settingsDao.insert(com.example.posdromex.data.database.entities.AppSettings())
        }

        // Add default units if empty
        val unitDao = database.unitDao()
        unitDao.getAllUnits().collect { units ->
            if (units.isEmpty()) {
                listOf(
                    com.example.posdromex.data.database.entities.Unit(name = "kg", symbol = "kg"),
                    com.example.posdromex.data.database.entities.Unit(name = "ton", symbol = "t"),
                    com.example.posdromex.data.database.entities.Unit(name = "cubic meter", symbol = "m³"),
                    com.example.posdromex.data.database.entities.Unit(name = "piece", symbol = "pc"),
                    com.example.posdromex.data.database.entities.Unit(name = "bag", symbol = "bag"),
                    com.example.posdromex.data.database.entities.Unit(name = "truck", symbol = "truck")
                ).forEach { unitDao.insert(it) }
            }
            return@collect
        }
    }
}

