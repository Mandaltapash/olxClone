package com.tods.project_olx.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ads")
data class AdEntity(
    @PrimaryKey
    val id: String,
    val state: String,
    val category: String,
    val title: String,
    val description: String,
    val value: String,
    val phone: String,
    val images: String,

    val timestamp: Long = 0L
)
