package com.jarnunes.udinetour.maps

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.jarnunes.udinetour.R

enum class PlaceType(val typeName: String, val icon: Int) {

    RESTAURANT("restaurant", R.drawable.map_location_restaurant),
    HOTEL("lodging", R.drawable.map_location_hotel),
    TOURIST_ATTRACTION("tourist_attraction", R.drawable.map_location_touristic);

    companion object {

        fun isTouristAttraction(value: String): Boolean {
            return TOURIST_ATTRACTION.typeName == value;
        }

        private fun fromValues(values: List<String>): PlaceType {
            return entries.firstOrNull { place -> values.any { value -> place.typeName == value } }
                ?: TOURIST_ATTRACTION
        }

        fun bitmapDescriptorFactory(values: List<String>): BitmapDescriptor {
            return BitmapDescriptorFactory.fromResource(fromValues(values).icon)
        }
    }
}