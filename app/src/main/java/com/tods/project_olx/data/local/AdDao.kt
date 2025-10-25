package com.tods.project_olx.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface AdDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAd(ad: AdEntity)

    @Query("SELECT * FROM ads ORDER BY timestamp DESC")
    suspend fun getAllAds(): List<AdEntity>

    @Query("SELECT * FROM ads WHERE id = :adId")
    suspend fun getAdById(adId: String): AdEntity?

    @Query("SELECT * FROM ads WHERE state = :region ORDER BY timestamp DESC")
    suspend fun getAdsByRegion(region: String): List<AdEntity>

    @Query("SELECT * FROM ads WHERE category = :category ORDER BY timestamp DESC")
    suspend fun getAdsByCategory(category: String): List<AdEntity>

    @Query("SELECT * FROM ads WHERE state = :region AND category = :category ORDER BY timestamp DESC")
    suspend fun getAdsByRegionAndCategory(region: String, category: String): List<AdEntity>

    @Query("SELECT * FROM ads WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchAds(query: String): List<AdEntity>

    @Query("DELETE FROM ads")
    suspend fun deleteAllAds()

    @Query("DELETE FROM ads WHERE timestamp < :timestamp")
    suspend fun deleteOldAds(timestamp: Long)

    @Delete
    suspend fun deleteAd(ad: AdEntity)
}