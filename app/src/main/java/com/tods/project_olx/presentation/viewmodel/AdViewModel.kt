package com.tods.project_olx.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tods.project_olx.model.Ad
import com.tods.project_olx.repository.AdRepository
import kotlinx.coroutines.launch

class AdViewModel(private val repository: AdRepository) : ViewModel() {

    private val _ads = MutableLiveData<Result<List<Ad>>>()
    val ads: LiveData<Result<List<Ad>>> = _ads

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchAds(region: String? = null, category: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getAds(region, category)
                _ads.value = Result.success(result)
            } catch (e: Exception) {
                _error.value = e.message
                _ads.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveAd(ad: Ad) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.saveAd(ad)
                _error.value = null
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
                repository.deleteAd(ad)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

// ViewModelFactory
class AdViewModelFactory(private val repository: AdRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}