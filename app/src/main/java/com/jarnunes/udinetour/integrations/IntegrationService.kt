package com.jarnunes.udinetour.integrations

import android.content.Context
import android.location.Location
import com.jarnunes.udinetour.R
import com.jarnunes.udinetour.integrations.dto.NearbyPlacesRequest
import com.jarnunes.udinetour.integrations.dto.PlacesResponse
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

    private suspend fun <T> invocation(callback: suspend (AwsFunctionService) -> T): T {
        val retrofit = createRetrofit()
        val apiService = retrofit.create(AwsFunctionService::class.java)
        return callback(apiService)
    }

    suspend fun answerQuestionAsync(request: QuestionRequest): QuestionResponse {
        return invocation{service -> service.answerQuestionAsync(request)}
    }

    suspend fun generateAudioDescriptionFromPlacesNameAsync(request: List<String>): TextToSpeechResponse {
        return invocation { service -> service.generateAudioDescriptionFromPlacesNameAsync(request) }
    }

    suspend fun getNearbyPlacesAsync(location: UserLocation): PlacesResponse {
        val request = NearbyPlacesRequest(location.latitude!!, location.longitude!!, 1000)
        return invocation { service -> service.getNearbyPlacesAsync(request) }
    }

}