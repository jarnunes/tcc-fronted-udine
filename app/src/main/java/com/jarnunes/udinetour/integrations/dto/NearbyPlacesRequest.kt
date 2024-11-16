package com.jarnunes.udinetour.integrations.dto

data class NearbyPlacesRequest(
    val latitude: Double,
    val longitude: Double,
    val radius: Int
)