package com.vijay.restaurant.domain

import retrofit2.HttpException
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.vijay.restaurant.data.ApiService
import com.vijay.restaurant.data.APIResponse
import com.vijay.restaurant.data.ErrorResponse
import com.vijay.restaurant.data.LocationNotFoundException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class APIRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun fetchRestaurants(
        location: String? = "New York City",
        radius: Int,
        offset: Int,
        apiKey: String
    ): APIResponse {
        val searchLocation = location ?: "New York City"

        return try {
            Timber.d("Making API call to fetch restaurants. ${location}")

            val response = apiService.searchRestaurants(
                location = searchLocation,
                radius = radius,
                offset = offset,
                authHeader = "Bearer $apiKey"
            )
            response
        } catch (e: HttpException) {
            if (e.code() == 400) {
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    if (errorResponse.error.code == "LOCATION_NOT_FOUND") {
                        throw LocationNotFoundException(errorResponse.error.description)
                    }else if (errorResponse.error.code == "VALIDATION_ERROR") {
                        throw LocationNotFoundException(errorResponse.error.description)
                    }
                } catch (jsonException: JsonSyntaxException) {
                    Timber.e(jsonException, "Error parsing error response JSON")
                }
            }
            throw e // Re-throw the exception if it's not a LOCATION_NOT_FOUND error
        } catch (e: Exception) {
            Timber.e(e, "Error during API call: ${e.message}")
            throw e
        }
    }
}
