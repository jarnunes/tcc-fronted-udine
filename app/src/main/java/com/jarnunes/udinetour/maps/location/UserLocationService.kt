package com.jarnunes.udinetour.maps.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.jarnunes.udinetour.maps.PlaceResult
import java.util.concurrent.TimeUnit

class UserLocationService(private val context: Context) {

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun getUserLocation(callback: (ArrayList<PlaceResult>) -> Unit){


    }

    private fun configureGetterForUserLocation() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION, false
                ) -> {
                    // Permissão concedida para localização precisa
                    getCurrentLocation()
                }

                permissions.getOrDefault(
                    Manifest.permission.ACCESS_COARSE_LOCATION, false
                ) -> {
                    // Permissão concedida para localização aproximada
                    getCurrentLocation()
                }

                else -> {
                    // Nenhuma permissão concedida
                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun getCurrentLocation() {
        val locationRequest = LocationRequest
            .Builder(TimeUnit.SECONDS.toMillis(30))
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5)).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                if (lastLocation != null) {
                    //TODO: pegando fixo temporariamente, pois no emulador nãoe está funcionando
                    // -19.918892780804857, -43.93867202055777

                    currentLocation.latitude = -19.918892780804857
                    currentLocation.longitude = -43.93867202055777
                    // currentLocation.latitude = lastLocation.latitude
                    // currentLocation.longitude = lastLocation.longitude
                }
            }
        }

        // Solicitar atualizações de localização
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper() // Para executar o callback na thread principal
        )
    }
}