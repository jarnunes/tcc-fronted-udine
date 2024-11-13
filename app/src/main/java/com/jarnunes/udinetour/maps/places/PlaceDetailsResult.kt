package com.jarnunes.udinetour.maps.places

data class PlaceDetailsResult(
    val name: String,
    val rating: Double,
    val reviews: List<Review>?
)