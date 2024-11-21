package com.jarnunes.udinetour.maps.location


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.jarnunes.udinetour.message.UserLocation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object UserLocationService2 {
    private lateinit var resultProvider: ActivityResultProvider
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private var isInitialized = false

    fun initialize(providerIn: ActivityResultProvider) {
        resultProvider = providerIn

        if (!isInitialized) {
            this.fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(resultProvider.getAppContext())

            // Criar e configurar a solicitação de permissões apenas uma vez
            locationPermissionRequest = resultProvider.getActivityResultLauncher(
                ActivityResultContracts.RequestMultiplePermissions(),
                ActivityResultCallback { permissions ->
                    if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    ) {
                        Toast.makeText(context(), "Permissões concedidas.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context(), "Permissão de localização negada.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            )

            isInitialized = true // Marca o serviço como inicializado
        }
    }

    private fun context(): Context {
        return resultProvider.getAppContext()
    }

    private fun fusedLocationProviderClient(): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context())
    }

    suspend fun getUserLocation(): UserLocation {
        val location = getLocation()
        val userLocation = UserLocation()
//        userLocation.latitude = location.latitude
//        userLocation.longitude = location.longitude
        userLocation.latitude = -19.92246722874205
        userLocation.longitude = -43.94447871534539
        return userLocation
    }

    suspend fun getLocation(): Location {
        // Verifica permissões de localização
        if (ActivityCompat.checkSelfPermission(
                context(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicita permissões, se necessário
            requestPermissions()
        }

        return suspendCancellableCoroutine { continuation ->
            fusedLocationProviderClient().lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(location)
                } else {
                    continuation.resumeWithException(Exception("Não foi possível obter a localização."))
                }
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }
    }

    private fun requestPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
//
//    private val locationPermissionLauncher by lazy {
//        resultProvider.getActivityResultLauncher(
//            ActivityResultContracts.RequestMultiplePermissions()
//        ) { permissions ->
//            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
//                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
//            ) {
//                Toast.makeText(context(), "Permissões concedidas.", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(context(), "Permissão de localização negada.", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }
//    }
}