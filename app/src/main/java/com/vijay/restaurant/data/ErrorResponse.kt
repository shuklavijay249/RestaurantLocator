package com.vijay.restaurant.data

data class ErrorResponse(val error: Error)

data class Error(val code: String, val description: String)

class LocationNotFoundException(message: String) : Exception(message)