package com.jarnunes.udinetour.integrations.dto

data class PlacesRequest(
    val locationRestriction: PlacesRequestRestriction,
    val maxResultCount: Int?,
    val includedTypes: List<String>?
)