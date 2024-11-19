package com.vijay.restaurant.data

data class APIResponse(
    val businesses: List<Business>,
    val total: Int
)

data class Business(
    val name: String,
    val distance: Double,
    val rating: Double,
    val location: Location,
    val image_url: String,
    val is_closed: Boolean,

    )

data class Location(
    val address1: String?,
    val address2: String?,
    val address3: String?,
    val city: String?,
    val zip_code: String?,
    val country: String?,
    val state: String?,
    val display_address: List<String>?
) {
    // Computed property to return a single string representation of the address
    val fullAddress: String
        get() {
            // Use a list to concatenate non-null fields
            val addressParts = mutableListOf<String>()

            address1?.let { addressParts.add(it) }
            address2?.let { addressParts.add(it) }
            address3?.let { addressParts.add(it) }
            city?.let { addressParts.add(it) }
            state?.let { addressParts.add(it) }
            zip_code?.let { addressParts.add(it) }

            // If 'display_address' is not null, use it
            display_address?.let {
                return it.joinToString(", ")
            }

            // If 'display_address' is null, return the manually calculated address
            return addressParts.joinToString(", ").takeIf { it.isNotEmpty() }
                ?: "Address not available"
        }
}

