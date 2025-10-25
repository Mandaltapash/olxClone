package com.tods.project_olx.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tods.project_olx.data.repository.AdRepository
import com.tods.project_olx.model.Ad
import com.tods.project_olx.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdViewModel @Inject constructor(
    private val repository: AdRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _ads = MutableLiveData<Resource<List<Ad>>>()
    val ads: LiveData<Resource<List<Ad>>> = _ads

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchAds(region: String? = null, category: String? = null) {
        viewModelScope.launch {
            repository.getAdsFlow(region, category)
                .catch { e ->
                    _error.postValue(e.message)
                    _ads.postValue(Resource.Error(e.message ?: "Unknown error"))
                }
                .collect { resource ->
                    _ads.postValue(resource)
                    _isLoading.postValue(resource is Resource.Loading)
                }
        }
    }

    fun saveAd(ad: Ad) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
                val result = repository.saveAd(ad, userId)

                when (result) {
                    is Resource.Success -> {
                        _error.value = null
                        fetchAds()
                    }
                    is Resource.Error -> {
                        _error.value = result.message
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAd(ad: Ad) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
                repository.deleteAd(ad, userId)
                fetchAds()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}