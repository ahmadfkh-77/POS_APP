package com.example.posdromex.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.posdromex.data.database.entities.ConversionRule
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversionRuleDao {
    @Query("SELECT * FROM conversion_rules WHERE isActive = 1 ORDER BY name ASC")
    fun getAllConversionRules(): Flow<List<ConversionRule>>

    @Query("SELECT * FROM conversion_rules WHERE id = :id")
    suspend fun getConversionRuleById(id: Long): ConversionRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: ConversionRule): Long

    @Update
    suspend fun update(rule: ConversionRule)

    @Delete
    suspend fun delete(rule: ConversionRule)
}
