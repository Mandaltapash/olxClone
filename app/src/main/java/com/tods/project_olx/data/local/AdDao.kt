package com.tods.project_olx.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tods.project_olx.data.local.AdEntity // <-- YEH LINE ADD KI HAI


@Dao
interface AdDao {

    /**
     * Naya Ad insert karta hai ya purane ko replace kar deta hai
     * agar primary key (id) same hai.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAd(ad: AdEntity)

    /**
     * Database se saare ads nikal kar list mein deta hai.
     */
    @Query("SELECT * FROM ads")
    suspend fun getAllAds(): List<AdEntity>

    /**
     * Ek specific Ad ko uski ID se dhoondhta hai.
     */
    @Query("SELECT * FROM ads WHERE id = :adId")
    suspend fun getAdById(adId: String): AdEntity?

    /**
     * Database se saare ads delete kar deta hai.
     */
    @Query("DELETE FROM ads")
    suspend fun deleteAllAds()
}

