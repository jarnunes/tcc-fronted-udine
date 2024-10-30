package com.jarnunes.udinetour.maps.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.concurrent.TimeUnit

class UserLocationService(
    private val context: Context,
    private val activityResultProvider: ActivityResultProvider) {

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun getUserLocation(callback:  (Location?) -> Unit) {
        val locationPermissionRequest = activityResultProvider.getActivityResultLauncher(
            ActivityResultContracts.RequestMultiplePermissions(),
            ActivityResultCallback { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                ) {
                    getLocation(callback)
                }
            }
        )

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(callback:  (Location?) -> Unit){

        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.requestLocationUpdates(
                createLocationRequest(),
                createLocationCallback(callback),
                Looper.getMainLooper() // Para executar o callback na thread principal
            )
        }
    }

    private fun hasPermission(resource: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            context,
            resource
        )
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest
            .Builder(TimeUnit.SECONDS.toMillis(30))
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5)).build()
    }

    private fun createLocationCallback(callback:  (Location?) -> Unit): LocationCallback {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                if (lastLocation != null) {
                    callback(lastLocation)
                }
            }
        }
        return locationCallback;
    }

}