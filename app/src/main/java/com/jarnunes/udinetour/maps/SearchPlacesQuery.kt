package com.jarnunes.udinetour.maps

import com.jarnunes.udinetour.message.UserLocation

data class SearchPlacesQuery(
    var userLocation: UserLocation,
    var radius: Int = 1000,
    var type: String = "tourist_attraction"
)