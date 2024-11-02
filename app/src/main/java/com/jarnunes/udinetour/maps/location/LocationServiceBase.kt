package com.jarnunes.udinetour.maps.location

import com.jarnunes.udinetour.MainActivity
import com.jarnunes.udinetour.message.UserLocation

abstract class LocationServiceBase(private var activity: MainActivity) {
    protected var locationService: UserLocationService = UserLocationService(activity)
    protected var userLocation: UserLocation = UserLocation()

    init {
        configureCurrentLocationCallback()
    }

    private fun configureCurrentLocationCallback() {
        locationService.getUserLocation { lastLocation ->
            val userLocation = UserLocation()
            userLocation.latitude = -19.918892780804857
            userLocation.longitude = -43.93867202055777
            this.userLocation = userLocation
            println(lastLocation)
        }
        //locationService.getCurrentLocation { this.userLocation = it }
    }
}