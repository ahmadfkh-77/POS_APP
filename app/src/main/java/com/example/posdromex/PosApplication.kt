package com.example.posdromex

import android.app.Application
import androidx.room.Room
import com.example.posdromex.data.database.PosDatabase

class PosApplication : Application() {
    val database: PosDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            PosDatabase::class.java,
            PosDatabase.DATABASE_NAME
        ).build()
    }

    companion object {
        lateinit var instance: PosApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

