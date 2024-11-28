package com.jarnunes.udinetour.maps

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.jarnunes.udinetour.MainActivity
import com.jarnunes.udinetour.R
import com.jarnunes.udinetour.commons.ILog
import com.jarnunes.udinetour.message.MapMessage

object MapService {

    private lateinit var mainActivity: MainActivity

    fun init(mainActivityIn: MainActivity) {
        mainActivity = mainActivityIn
    }

    fun removeDefaultConfiguration(googleMap: GoogleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mainActivity, R.raw.map_style))
        googleMap.uiSettings.isZoomControlsEnabled = true // Adicionar controles de zoom
        googleMap.uiSettings.isScrollGesturesEnabled = true // Permitir gestos de rolagem
        googleMap.uiSettings.isTiltGesturesEnabled = true // Permitir gestos de inclinação
        googleMap.uiSettings.isRotateGesturesEnabled = true // Permitir gestos de rotação
    }

    fun addCurrentLocationPointMarker(googleMap: GoogleMap, message: MapMessage) {
        val userLocation = message.userLocation
        val location = LatLng(userLocation?.latitude!!, userLocation.longitude!!)
        val title = mainActivity.getString(R.string.maps_current_location)
        googleMap.addMarker(MarkerOptions().position(location).title(title))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    fun addTouristsPointMarker(googleMap: GoogleMap, message: MapMessage) {
        message.places.forEach { place ->
            try {

                val placeLoc = place.location
                val customIcon = PlaceType.bitmapDescriptorFactory(place.types)
                val loc = LatLng(placeLoc.latitude, placeLoc.longitude)
                val marker = googleMap.addMarker(
                    MarkerOptions().position(loc).title(place.displayName.text).icon(customIcon)
                )
                marker?.showInfoWindow()


            } catch (exception: Exception) {
                ILog.e(ILog.MAP_SERVICE, exception)
            }
        }
    }
}