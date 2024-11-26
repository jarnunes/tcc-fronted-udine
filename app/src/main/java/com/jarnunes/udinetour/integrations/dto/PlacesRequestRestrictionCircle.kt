package com.jarnunes.udinetour.integrations.dto

data class PlacesRequestRestrictionCircle(
    val center: PlacesRequestRestrictionCenter,
    val radius: Double?
)