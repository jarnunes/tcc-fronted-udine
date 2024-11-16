package com.jarnunes.udinetour.integrations.dto

data class Place(
    var place_id: String,
    val name: String,
    val vicinity: String,
    val geometry: Geometry,
    val types: List<String>,
    val icon: String
)