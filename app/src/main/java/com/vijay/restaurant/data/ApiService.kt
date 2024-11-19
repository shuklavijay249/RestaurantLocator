package com.vijay.restaurant.data

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {
    @GET("businesses/search")
    suspend fun searchRestaurants(
        @Query("term") term: String = "restaurants",
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("sort_by") sortBy: String = "distance",
        @Query("limit") limit: Int = 15,
        @Query("offset") offset: Int,
        @Header("Authorization") authHeader: String
    ): APIResponse
}
