package com.jarnunes.udinetour.maps

import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.jarnunes.udinetour.MainActivity
import com.jarnunes.udinetour.R
import com.jarnunes.udinetour.message.MapMessage

class MapService(
    private val containerViewId: Int,
    private val mainActivity: MainActivity,
    private val fragmentManager: FragmentManager
) {

    fun createMap(message: MapMessage) {
        val mapFragment = SupportMapFragment.newInstance()
        fragmentManager.beginTransaction()
            .replace(containerViewId, mapFragment)
            .commit()

        mapFragment.getMapAsync { googleMap ->
            removeDefaultConfiguration(googleMap)
            addCurrentLocationPointMarker(googleMap, message)
            addTouristsPointMarker(googleMap, message)
        }
    }

    private fun removeDefaultConfiguration(googleMap: GoogleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mainActivity, R.raw.map_style))
    }

    private fun addCurrentLocationPointMarker(googleMap: GoogleMap, message: MapMessage) {
        val userLocation = message.getUserLocation()
        val location = LatLng(userLocation.latitude!!, userLocation.longitude!!)
        val title = mainActivity.getString(R.string.maps_current_location)
        googleMap.addMarker(MarkerOptions().position(location).title(title))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun addTouristsPointMarker(googleMap: GoogleMap, message: MapMessage) {
        message.places.forEach { place ->
            val placeLoc = place.geometry.location
            val customIcon = PlaceType.bitmapDescriptorFactory(place.types)
            val loc = LatLng(placeLoc.lat, placeLoc.lng)
            val marker = googleMap.addMarker(
                MarkerOptions().position(loc).title(place.name).icon(customIcon)
            )
            marker?.showInfoWindow()
        }
    }
}