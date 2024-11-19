package com.jarnunes.udinetour.maps.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.jarnunes.udinetour.message.UserLocation
import java.util.concurrent.TimeUnit

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

    fun getCurrentLocation(callback: (UserLocation) -> Unit) {
        if (!isInitialized) {
            throw IllegalStateException("UserLocationService must be initialized before use.")
        }

        getUserLocation(callback)
    }

    // Função para solicitar permissões se necessário
    private fun getUserLocation(callback: (UserLocation) -> Unit) {
        // Verifica se as permissões já foram concedidas
        val locationPermissionsGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (locationPermissionsGranted) {
            // Se as permissões foram concedidas, obtemos a localização
            getLocation(callback)
        } else {
            // Caso contrário, solicita permissões
            locationPermissionRequest?.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Função para obter a localização a partir do FusedLocationClient
    @SuppressLint("MissingPermission")
    private fun getLocation(callback: (UserLocation) -> Unit) {
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.requestLocationUpdates(
                createLocationRequest(),
                createLocationCallback(callback), // Passando o callback aqui
                Looper.getMainLooper() // Para executar o callback na thread principal
            )
        }
    }

    private fun hasPermission(resource: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            activityResultProvider.getAppContext(),
            resource
        )
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest
            .Builder(TimeUnit.SECONDS.toMillis(30))
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5))
            .build()
    }

    private fun createLocationCallback(callback: (UserLocation) -> Unit): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                val userLocation = UserLocation().apply {
                    latitude = lastLocation?.latitude ?: -19.918892780804857
                    longitude = lastLocation?.longitude ?: -43.93867202055777
                }
                lastUserLocation = userLocation
                callback(userLocation)
            }
        }
    }
}
