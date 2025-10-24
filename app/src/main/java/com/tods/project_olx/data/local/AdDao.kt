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
    val images: String, // JSON string of image URLs
    val timestamp: Long = System.currentTimeMillis()
)

