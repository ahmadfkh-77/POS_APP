package com.example.posdromex.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.posdromex.data.database.entities.AppSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsSync(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: AppSettings)

    @Update
    suspend fun update(settings: AppSettings)

    @Query("UPDATE app_settings SET nextReceiptNumber = nextReceiptNumber + 1 WHERE id = 1")
    suspend fun incrementReceiptNumber()

    @Query("UPDATE app_settings SET nextDeliveryAuthNumber = nextDeliveryAuthNumber + 1 WHERE id = 1")
    suspend fun incrementDeliveryAuthNumber()

    @Query("UPDATE app_settings SET printerMacAddress = :macAddress, printerName = :name WHERE id = 1")
    suspend fun updatePrinter(macAddress: String, name: String)
}
