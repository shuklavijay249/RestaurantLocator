package com.vijay.restaurant.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijay.restaurant.domain.APIRepository
import com.vijay.restaurant.data.Business
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantViewModel @Inject constructor(private val repository: APIRepository) : ViewModel() {

    // Use StateFlow to represent the loading and restaurant list states
    private val _restaurants = MutableStateFlow<List<Business>>(emptyList())
    val restaurants: StateFlow<List<Business>> get() = _restaurants

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private var offset = 0
    private var isLastPage = false

    fun clearRestaurants() {
        _restaurants.value = emptyList()
        offset = 0
        isLastPage = false
    }

    fun resetError() {
        _error.value = null
    }


    fun loadRestaurants(radius: Int, apiKey: String) {
        loadRestaurants("restaurants", "New York City", radius, apiKey)
    }

    fun loadRestaurants(term: String?, location: String?, radius: Int, apiKey: String) {
        if (_isLoading.value || isLastPage) return
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = repository.fetchRestaurants(location, radius, offset, apiKey)
                val currentList = _restaurants.value.toMutableList()
                currentList.addAll(response.businesses)
                _restaurants.value = currentList

                isLastPage = response.businesses.isEmpty()
                offset += response.businesses.size
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchRestaurants(location: String, radius: Int, apiKey: String) {
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch {
            try {
                clearRestaurants()
                val response = repository.fetchRestaurants(location, radius, offset, apiKey)

                _restaurants.value = response.businesses // Update with new data
                isLastPage = response.businesses.isEmpty()
                offset = response.businesses.size
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

}
