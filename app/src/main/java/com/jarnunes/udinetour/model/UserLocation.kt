package com.jarnunes.udinetour.model

import java.io.Serializable

class UserLocation(latitude: Double?, longitude: Double?) : Serializable {
    var latitude: Double? = latitude
    var longitude: Double? = longitude

    constructor() : this(null, null) {}
}