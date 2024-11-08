package com.jarnunes.udinetour.maps

data class PlaceResult(
    val name: String,
    val vicinity: String,
    val geometry: Geometry,
    val types: List<String>
)