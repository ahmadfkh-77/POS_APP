package com.example.posdromex.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.posdromex.data.database.entities.Truck
import kotlinx.coroutines.flow.Flow

@Dao
interface TruckDao {
    @Query("SELECT * FROM trucks WHERE isActive = 1 ORDER BY plateNumber ASC")
    fun getAllTrucks(): Flow<List<Truck>>

    @Query("SELECT * FROM trucks ORDER BY plateNumber ASC")
    fun getAllTrucksIncludingInactive(): Flow<List<Truck>>

    @Query("SELECT * FROM trucks WHERE id = :id")
    suspend fun getTruckById(id: Long): Truck?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(truck: Truck): Long

    @Update
    suspend fun update(truck: Truck)

    @Delete
    suspend fun delete(truck: Truck)
}
