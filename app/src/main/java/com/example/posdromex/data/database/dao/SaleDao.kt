package com.example.posdromex.data.database.dao

import androidx.room.*
import com.example.posdromex.data.database.entities.Sale
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY date DESC")
    fun getSalesByCustomerId(customerId: Long): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE customerId = :customerId AND type = :type ORDER BY date DESC")
    fun getSalesByTypeAndCustomer(customerId: Long, type: String): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): Sale?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Update
    suspend fun updateSale(sale: Sale)

    @Delete
    suspend fun deleteSale(sale: Sale)

    @Query("UPDATE sales SET receiptPrintCount = receiptPrintCount + 1 WHERE id = :saleId")
    suspend fun incrementReceiptPrintCount(saleId: Long)

    @Query("UPDATE sales SET deliveryAuthPrintCount = deliveryAuthPrintCount + 1 WHERE id = :saleId")
    suspend fun incrementDeliveryAuthPrintCount(saleId: Long)
}

