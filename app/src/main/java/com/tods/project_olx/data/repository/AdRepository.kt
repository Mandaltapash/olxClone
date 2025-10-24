package com.tods.project_olx.repository

import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.tods.project_olx.data.local.AdDao
import com.tods.project_olx.data.local.toAd
import com.tods.project_olx.data.local.toEntity
import com.tods.project_olx.model.Ad
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class AdRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val adDao: AdDao
) {
    private val adsRef = database.getReference("ads")
    private val myAdsRef = database.getReference("my_adds")

    // Get ads with offline support
    suspend fun getAds(region: String? = null, category: String? = null): List<Ad> {
        return try {
            // Try to fetch from network
            val networkAds = fetchFromNetwork(region, category)

            // Cache in local database
            adDao.insertAll(networkAds.map { it.toEntity() })

            networkAds
        } catch (e: Exception) {
            // Fallback to local cache
            getFromCache(region, category)
        }
    }

    private suspend fun fetchFromNetwork(region: String?, category: String?): List<Ad> {
        val query = when {
            region != null && category != null -> adsRef.child(region).child(category)
            region != null -> adsRef.child(region)
            else -> adsRef
        }

        val snapshot = query.get().await()
        val adsList = mutableListOf<Ad>()

        snapshot.children.forEach { regionSnapshot ->
            if (category != null || region != null) {
                regionSnapshot.children.forEach { adSnapshot ->
                    adSnapshot.getValue(Ad::class.java)?.let { adsList.add(it) }
                }
            } else {
                regionSnapshot.children.forEach { categorySnapshot ->
                    categorySnapshot.children.forEach { adSnapshot ->
                        adSnapshot.getValue(Ad::class.java)?.let { adsList.add(it) }
                    }
                }
            }
        }
        return adsList.reversed()
    }

    private suspend fun getFromCache(region: String?, category: String?): List<Ad> {
        val flow = when {
            region != null && category != null -> adDao.getAdsByRegionAndCategory(region, category)
            region != null -> adDao.getAdsByRegion(region)
            category != null -> adDao.getAdsByCategory(category)
            else -> adDao.getAllAds()
        }

        // Convert Flow to List (take first emission)
        var cachedAds: List<Ad> = emptyList()
        flow.collect { entities ->
            cachedAds = entities.map { it.toAd() }
        }
        return cachedAds
    }

    // Observe ads in real-time
    fun observeAds(region: String? = null): Flow<List<Ad>> = callbackFlow {
        val query = if (region != null) adsRef.child(region) else adsRef

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adsList = mutableListOf<Ad>()
                snapshot.children.forEach { categorySnapshot ->
                    categorySnapshot.children.forEach { adSnapshot ->
                        adSnapshot.getValue(Ad::class.java)?.let { adsList.add(it) }
                    }
                }
                trySend(adsList.reversed())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun saveAd(ad: Ad, userId: String) {
        try {
            // Save to Firebase
            myAdsRef.child(userId).child(ad.id).setValue(ad).await()
            adsRef.child(ad.state).child(ad.category).child(ad.id).setValue(ad).await()

            // Save to local cache
            adDao.insertAd(ad.toEntity())
        } catch (e: Exception) {
            throw Exception("Failed to save ad: ${e.message}")
        }
    }

    suspend fun deleteAd(ad: Ad, userId: String) {
        try {
            myAdsRef.child(userId).child(ad.id).removeValue().await()
            adsRef.child(ad.state).child(ad.category).child(ad.id).removeValue().await()
            adDao.deleteAd(ad.toEntity())
        } catch (e: Exception) {
            throw Exception("Failed to delete ad: ${e.message}")
        }
    }

    suspend fun uploadImage(imageUri: android.net.Uri, adId: String, index: Int): String {
        return try {
            val ref = storage.reference.child("images/ads/$adId/image$index.jpg")
            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw Exception("Failed to upload image: ${e.message}")
        }
    }

    suspend fun getUserAds(userId: String): List<Ad> {
        return try {
            val snapshot = myAdsRef.child(userId).get().await()
            snapshot.children.mapNotNull { it.getValue(Ad::class.java) }.reversed()
        } catch (e: Exception) {
            throw Exception("Failed to fetch user ads: ${e.message}")
        }
    }

    // Search functionality
    fun searchAds(query: String): Flow<List<Ad>> {
        return adDao.searchAds(query).map { entities ->
            entities.map { it.toAd() }
        }
    }

    // Clean old cache (older than 7 days)
    suspend fun cleanOldCache() {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        adDao.deleteOldAds(sevenDaysAgo)
    }
}