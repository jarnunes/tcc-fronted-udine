package com.jarnunes.udinetour.maps

data class PlaceResult(
    var place_id: String,
    val name: String,
    val vicinity: String,
    val geometry: Geometry,
    val types: List<String>
)