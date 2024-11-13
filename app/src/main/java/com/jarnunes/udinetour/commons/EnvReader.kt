package com.jarnunes.udinetour.commons

enum class EnvReader {

    GOOGLE_PLACES_API_KEY;

    companion object {
        fun googlePlacesApiKey(): String {
            return System.getenv(GOOGLE_PLACES_API_KEY.toString())!!
        }
    }
}