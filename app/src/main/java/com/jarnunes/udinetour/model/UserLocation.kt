package com.jarnunes.udinetour.model

import java.io.Serializable

class UserLocation(latitude: String?, longitude: String?) : Serializable {
    var latitude: String? = latitude
    var longitude: String? = longitude

    constructor() : this(null, null) {}
}