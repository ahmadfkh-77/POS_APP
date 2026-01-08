package com.example.posdromex.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.posdromex.data.database.entities.DeliveryInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryInfoDao {
    @Query("SELECT * FROM delivery_info WHERE saleId = :saleId")
    suspend fun getDeliveryInfoBySaleId(saleId: Long): DeliveryInfo?

    @Query("SELECT * FROM delivery_info WHERE saleId = :saleId")
    fun getDeliveryInfoBySaleIdFlow(saleId: Long): Flow<DeliveryInfo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deliveryInfo: DeliveryInfo): Long

    @Update
    suspend fun update(deliveryInfo: DeliveryInfo)

    @Delete
    suspend fun delete(deliveryInfo: DeliveryInfo)
}
