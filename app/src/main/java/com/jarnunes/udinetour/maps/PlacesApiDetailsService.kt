package com.jarnunes.udinetour.maps


import com.jarnunes.udinetour.maps.places.PlaceDetailsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiDetailsService {

    @GET("details/json")
    fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String
    ): Call<PlaceDetailsResponse>
}