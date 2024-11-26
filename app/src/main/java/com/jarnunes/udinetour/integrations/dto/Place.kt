package com.jarnunes.udinetour.integrations.dto

import java.io.Serializable

data class Place(
    var id: String,
    val displayName: PlacesText,
    val shortFormattedAddress: String,
    val location: Location,
    val types: List<String>,
    val iconMaskBaseUri: String

) : Serializable