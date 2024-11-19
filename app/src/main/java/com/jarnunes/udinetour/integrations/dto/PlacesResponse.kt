package com.jarnunes.udinetour.integrations.dto

import java.io.Serializable

data class PlacesResponse(
    val results: ArrayList<Place>
) : Serializable