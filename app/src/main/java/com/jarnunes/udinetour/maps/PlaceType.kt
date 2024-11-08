package com.jarnunes.udinetour.maps

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.jarnunes.udinetour.R

enum class PlaceType(val typeName: String, val icon: Int) {
    CHURCH("church", R.drawable.map_location_church),
    MUSEUM("museum", R.drawable.map_location_touristic),
    PARK("park", R.drawable.map_location_touristic),
    MOVIE_THEATER("movie_theater", R.drawable.map_location_touristic),
    TOURIST_ATTRACTION("tourist_attraction", R.drawable.map_location_touristic);

    companion object {
        fun isChurch(value: String): Boolean {
            return CHURCH.typeName == value
        }

        fun isMuseum(value: String): Boolean {
            return MUSEUM.typeName == value;
        }

        fun isPark(value: String): Boolean {
            return PARK.typeName == value;
        }

        fun isMovieTheater(value: String): Boolean {
            return MOVIE_THEATER.typeName == value;
        }

        fun isTouristAttraction(value: String): Boolean {
            return TOURIST_ATTRACTION.typeName == value;
        }

        fun fromValues(values: List<String>): PlaceType {
            return entries.first() { place -> values.any { value -> place.typeName == value } }
        }

        fun bitmapDescriptorFactory(values: List<String>): BitmapDescriptor {
            return BitmapDescriptorFactory.fromResource(fromValues(values).icon)
        }
    }
}