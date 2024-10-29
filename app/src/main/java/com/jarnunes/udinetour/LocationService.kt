package com.jarnunes.udinetour

import com.google.android.gms.location.FusedLocationProviderClient
import com.jarnunes.udinetour.message.UserLocation

class LocationService {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: UserLocation

}