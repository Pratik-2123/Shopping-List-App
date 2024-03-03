package com.example.myshoppinglist

data class LocationData(
    val latitude : Double,
    val longitude : Double
)

data class GeocodingResponse(
    val results : List<GeocodingResults>,
    val status : String
)

data class GeocodingResults(
    val formatted_address : String
)

