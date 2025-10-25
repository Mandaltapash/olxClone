package com.tods.project_olx.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// AdEntity ko import karein, agar zaroorat ho
import com.tods.project_olx.data.local.AdEntity


@Database(
    entities = [AdEntity::class], 
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun adDao(): AdDao
}

