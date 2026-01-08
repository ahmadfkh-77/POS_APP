package com.example.posdromex.data.database.dao

import androidx.room.*
import com.example.posdromex.data.database.entities.SaleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleItemDao {
    @Query("SELECT * FROM sale_items WHERE saleId = :saleId ORDER BY id ASC")
    fun getItemsBySaleId(saleId: Long): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items WHERE id = :id")
    suspend fun getItemById(id: Long): SaleItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SaleItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<SaleItem>)

    @Update
    suspend fun updateItem(item: SaleItem)

    @Delete
    suspend fun deleteItem(item: SaleItem)
}

