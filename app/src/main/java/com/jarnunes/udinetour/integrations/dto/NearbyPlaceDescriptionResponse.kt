package com.jarnunes.udinetour.integrations.dto

data class NearbyPlaceDescriptionResponse(
    var places: List<Place> = emptyList(),
    var audioDescriptionContent: String
)