package com.jarnunes.udinetour

import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.FusedLocationProviderClient
import com.jarnunes.udinetour.model.UserLocation

class LocationService {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: UserLocation

}