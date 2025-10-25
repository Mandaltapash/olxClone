package com.tods.project_olx.data.repository // ✅ PACKAGE SAHI KIYA

import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tods.project_olx.data.local.AdDao
import com.tods.project_olx.data.local.AdEntity
import com.tods.project_olx.model.Ad
import com.tods.project_olx.utils.Resource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val adDao: AdDao
) {
    private val adsRef = database.getReference("ads")
    private val myAdsRef = database.getReference("my_adds")

    // Get ads with offline support using Flow
    fun getAdsFlow(region: String? = null, category: String? = null): Flow<Resource<List<Ad>>> = flow {
        emit(Resource.Loading())

        try {
            // First emit cached data
            val cachedAds = getFromCache(region, category)
            if (cachedAds.isNotEmpty()) {
                emit(Resource.Success(cachedAds))
            }

            // Then try to fetch from network
            val networkAds = fetchFromNetwork(region, category)

            // Cache in local database
            networkAds.forEach { ad ->
                adDao.insertAd(ad.toEntity())
            }

            emit(Resource.Success(networkAds))
        } catch (e: Exception) {
            // If network fails, return cached data
            val cachedAds = getFromCache(region, category)
            if (cachedAds.isNotEmpty()) {
                emit(Resource.Success(cachedAds))
            } else {
                emit(Resource.Error(e.message ?: "Unknown error occurred"))
            }
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
        val entities = adDao.getAllAds() // Now returns List<AdEntity>
        return entities.filter { entity ->
            when {
                region != null && category != null ->
                    entity.state == region && entity.category == category
                region != null -> entity.state == region
                category != null -> entity.category == category
                else -> true
            }
        }.map { it.toAd() }
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

    suspend fun saveAd(ad: Ad, userId: String): Resource<Unit> {
        return try {
            // Save to Firebase
            myAdsRef.child(userId).child(ad.id).setValue(ad).await()
            adsRef.child(ad.state).child(ad.category).child(ad.id).setValue(ad).await()

            // Save to local cache
            adDao.insertAd(ad.toEntity())

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save ad")
        }
    }

    suspend fun deleteAd(ad: Ad, userId: String): Resource<Unit> {
        return try {
            myAdsRef.child(userId).child(ad.id).removeValue().await()
            adsRef.child(ad.state).child(ad.category).child(ad.id).removeValue().await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete ad")
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
}

// Extension functions
fun Ad.toEntity(): AdEntity {
    return AdEntity(
        id = this.id,
        state = this.state,
        category = this.category,
        title = this.title,
        description = this.description,
        value = this.value,
        phone = this.phone,
        images = this.adImages.joinToString(","),
        timestamp = System.currentTimeMillis()
    )
}

fun AdEntity.toAd(): Ad {
    return Ad(
        id = this.id,
        state = this.state,
        category = this.category,
        title = this.title,
        description = this.description,
        value = this.value,
        phone = this.phone,
        adImages = this.images.split(",").filter { it.isNotEmpty() }
    )
}