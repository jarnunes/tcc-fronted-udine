package com.jarnunes.udinetour.integrations

import com.jarnunes.udinetour.integrations.dto.NearbyPlaceDescriptionResponse
import com.jarnunes.udinetour.integrations.dto.PlacesRequest
import com.jarnunes.udinetour.integrations.dto.QuestionRequest
import com.jarnunes.udinetour.integrations.dto.QuestionResponse
import com.jarnunes.udinetour.integrations.dto.TextToSpeechResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface UdineApiService {

    @POST("/places/searchNearby")
    suspend fun getNearbyPlacesAsync(@Body request: PlacesRequest): NearbyPlaceDescriptionResponse

    @POST("/text-to-speech/short-description-from-places")
    suspend fun generateAudioDescriptionFromPlacesNameAsync(@Body request: List<String>): TextToSpeechResponse

    @POST("/answer/places-question")
    suspend fun answerQuestionAsync(@Body request: QuestionRequest): QuestionResponse

}