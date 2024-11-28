package com.jarnunes.udinetour.integrations

import android.content.Context
import com.jarnunes.udinetour.R
import com.jarnunes.udinetour.integrations.dto.NearbyPlaceDescriptionResponse
import com.jarnunes.udinetour.integrations.dto.PlacesRequest
import com.jarnunes.udinetour.integrations.dto.PlacesRequestRestriction
import com.jarnunes.udinetour.integrations.dto.PlacesRequestRestrictionCenter
import com.jarnunes.udinetour.integrations.dto.PlacesRequestRestrictionCircle
import com.jarnunes.udinetour.integrations.dto.QuestionRequest
import com.jarnunes.udinetour.integrations.dto.QuestionResponse
import com.jarnunes.udinetour.integrations.dto.TextToSpeechResponse
import com.jarnunes.udinetour.message.UserLocation
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class IntegrationService(val context: Context) {

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .client(configureHTTPClient())
            .baseUrl(context.getString(R.string.url_udine_aws_function))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun configureHTTPClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    }

    private suspend fun <T> invocation(callback: suspend (UdineApiService) -> T): T {
        val retrofit = createRetrofit()
        val apiService = retrofit.create(UdineApiService::class.java)
        return callback(apiService)
    }

    suspend fun answerQuestionAsync(request: QuestionRequest): QuestionResponse {
        val retrofit = createRetrofit()
        val apiService = retrofit.create(UdineApiService::class.java)
        return apiService.answerQuestionAsync(request)

    }

    suspend fun generateAudioDescriptionFromPlacesNameAsync(request: List<String>): TextToSpeechResponse {
        val retrofit = createRetrofit()
        val apiService = retrofit.create(UdineApiService::class.java)
        return apiService.generateAudioDescriptionFromPlacesNameAsync(request)
    }

    suspend fun getNearbyPlacesAsync(location: UserLocation): NearbyPlaceDescriptionResponse {
        val center = PlacesRequestRestrictionCenter(location.latitude!!, location.longitude!!)
        val circle = PlacesRequestRestrictionCircle(center, null)
        val restriction = PlacesRequestRestriction(circle)
        val request = PlacesRequest(restriction, null, null)

        val retrofit = createRetrofit()
        val apiService = retrofit.create(UdineApiService::class.java)
        return apiService.getNearbyPlacesAsync(request)
    }

}