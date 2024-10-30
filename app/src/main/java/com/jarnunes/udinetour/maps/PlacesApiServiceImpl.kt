package com.jarnunes.udinetour.maps

import android.content.Context
import android.util.Log
import com.jarnunes.udinetour.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlacesApiServiceImpl(private val context: Context) {

    fun getNearbyPlaces(
        placesQuery: SearchPlacesQuery,
        callback: (ArrayList<PlaceResult>) -> Unit
    ) {
        val retrofit = Retrofit.Builder()
            .baseUrl(context.getString(R.string.google_places_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(PlacesApiService::class.java)
        val location = placesQuery.userLocation
        val locationString = "${location.latitude},${location.longitude}"
        val call = apiService.getNearbyPlaces(
            locationString,
            placesQuery.radius,
            placesQuery.type,
            context.getString(R.string.google_maps_api_key)
        )

        call.enqueue(object : Callback<PlacesResponse> {
            override fun onResponse(
                call: Call<PlacesResponse>,
                response: Response<PlacesResponse>
            ) {
                if (response.isSuccessful) {
                    val placesResult = ArrayList<PlaceResult>()
                    val places = response.body()?.results
                    places?.forEach { place ->
                        placesResult.add(place)
                    }
                    // Chama a callback com os resultados
                    callback(placesResult)
                } else {
                    Log.e("API Error", "Response unsuccessful: ${response.errorBody()?.string()}")
                    // Retorna lista vazia em caso de erro
                    callback(ArrayList())
                }
            }

            override fun onFailure(call: Call<PlacesResponse>, t: Throwable) {
                Log.e("API Error", "Failed to fetch places", t)
                // Retorna lista vazia em caso de falha
                callback(ArrayList())
            }
        })
    }
}