package com.example.posdromex.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.posdromex.data.database.entities.Unit
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Query("SELECT * FROM units WHERE isActive = 1 ORDER BY name ASC")
    fun getAllUnits(): Flow<List<Unit>>

    @Query("SELECT * FROM units WHERE id = :id")
    suspend fun getUnitById(id: Long): Unit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(unit: Unit): Long

    @Update
    suspend fun update(unit: Unit)

    @Delete
    suspend fun delete(unit: Unit)
}
