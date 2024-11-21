package com.jarnunes.udinetour.integrations

import com.jarnunes.udinetour.integrations.dto.NearbyPlacesRequest
import com.jarnunes.udinetour.integrations.dto.PlacesResponse
import com.jarnunes.udinetour.integrations.dto.QuestionRequest
import com.jarnunes.udinetour.integrations.dto.QuestionResponse
import com.jarnunes.udinetour.integrations.dto.TextToSpeechResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AwsFunctionService {

    @POST("/nearbyPlaces")
    @Headers("spring.cloud.function.definition: nearbyPlaces")
    suspend fun getNearbyPlacesAsync(@Body request: NearbyPlacesRequest): PlacesResponse

    @POST("/generateShortAudioDescriptionFromPlacesName")
    @Headers("spring.cloud.function.definition: generateShortAudioDescriptionFromPlacesName")
    suspend fun generateAudioDescriptionFromPlacesNameAsync(@Body request: List<String>): TextToSpeechResponse

    @POST("/answerQuestion")
    @Headers("spring.cloud.function.definition: answerQuestion")
    suspend fun answerQuestionAsync(@Body request: QuestionRequest): QuestionResponse

}