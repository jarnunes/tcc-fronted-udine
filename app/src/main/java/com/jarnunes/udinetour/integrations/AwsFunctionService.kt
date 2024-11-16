package com.jarnunes.udinetour.integrations

import com.jarnunes.udinetour.integrations.dto.NearbyPlacesRequest
import com.jarnunes.udinetour.integrations.dto.PlacesResponse
import com.jarnunes.udinetour.integrations.dto.SynthesizeTextResponse
import com.jarnunes.udinetour.integrations.dto.TextToSpeechResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AwsFunctionService {

    @POST("/")
    @Headers("spring.cloud.function.definition: nearbyPlaces")
    fun getNearbyPlaces(@Body request: NearbyPlacesRequest): Call<PlacesResponse>

    @POST("/")
    @Headers("spring.cloud.function.definition: describeLocation")
    fun describeLocation(@Body request: String): Call<String>

    @POST("/")
    @Headers("spring.cloud.function.definition: describeLocations")
    fun describeLocations(@Body request: List<String>): Call<String>

    @POST("/")
    @Headers("spring.cloud.function.definition: textToSpeech")
    fun synthesizeText(@Body request: String): Call<SynthesizeTextResponse>

    @POST("/")
    @Headers("spring.cloud.function.definition: speechToText")
    fun recognize(@Body request: String): Call<String>

    @POST("/")
    @Headers("spring.cloud.function.definition: generateAudioDescriptionFromPlacesName")
    fun generateAudioDescriptionFromPlacesName(@Body request: List<String>): Call<TextToSpeechResponse>
}