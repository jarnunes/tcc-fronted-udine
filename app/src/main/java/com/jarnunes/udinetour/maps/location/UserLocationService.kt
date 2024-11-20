package com.jarnunes.udinetour.maps.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.jarnunes.udinetour.message.UserLocation
import kotlinx.coroutines.tasks.await

object UserLocationService {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var activityResultProvider: ActivityResultProvider
    private var isInitialized = false
    private var locationPermissionRequest: ActivityResultLauncher<Array<String>>? = null
    var lastUserLocation: UserLocation? = null

    fun initialize(activityResultProvider: ActivityResultProvider) {
        if (!isInitialized) {
            this.activityResultProvider = activityResultProvider
            this.fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(activityResultProvider.getAppContext())

            // Criar e configurar a solicitação de permissões apenas uma vez
            locationPermissionRequest = activityResultProvider.getActivityResultLauncher(
                ActivityResultContracts.RequestMultiplePermissions(),
                ActivityResultCallback { permissions ->
                    if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    ) {
                        // Permissões concedidas, podemos pegar a localização
                    }
                }
            )

            isInitialized = true // Marca o serviço como inicializado
        }
    }

    private fun hasPermission(resource: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            activityResultProvider.getAppContext(),
            resource
        )
    }

    suspend fun getCurrentLocationAsync(): UserLocation {
        if (!isInitialized) {
            throw IllegalStateException("UserLocationService must be initialized before use.")
        }
        return getLocation()
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocation(): UserLocation {
        val locationPermissionsGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            && hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (locationPermissionsGranted) {
            return try {
                val location = fusedLocationClient.lastLocation.await()
                UserLocation().apply {
                    latitude = location?.latitude
                    longitude = location?.longitude
                }
            } catch (e: Exception) {
                throw RuntimeException("Error getting location", e)
            }
        } else {
            locationPermissionRequest?.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return getLocation()
        }
    }
}
