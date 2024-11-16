package com.jarnunes.udinetour.integrations

import android.content.Context
import androidx.core.util.Function
import com.jarnunes.udinetour.R
import com.jarnunes.udinetour.commons.ILog
import com.jarnunes.udinetour.integrations.dto.NearbyPlacesRequest
import com.jarnunes.udinetour.integrations.dto.PlacesResponse
import com.jarnunes.udinetour.integrations.dto.SynthesizeTextResponse
import com.jarnunes.udinetour.integrations.dto.TextToSpeechResponse
import com.jarnunes.udinetour.message.UserLocation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class IntegrationService(val context: Context) {

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(context.getString(R.string.url_udine_aws_function))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Executes the request in parallel
     */
    private fun <T> enqueue(call: Call<T>, successCallback: (T?) -> Unit,
        errorCallback: (Response<T>) -> Unit, onFailure: (Throwable) -> Unit) {
        try {
            call.enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        successCallback(response.body())
                    } else {
                        val msg = "Response unsuccessful: ${response.errorBody()?.string()}"
                        ILog.e(ILog.INTEGRATION_SERVICE, msg)
                        errorCallback(response)
                    }
                }

                override fun onFailure(p0: Call<T>, t: Throwable) {
                    ILog.e(ILog.INTEGRATION_SERVICE, t)
                    onFailure(t)
                }
            })

        } catch (e: Exception) {
            ILog.e(ILog.INTEGRATION_SERVICE, e)
            onFailure(e)
        }
    }

    fun <R> invoke(callback: Function<AwsFunctionService, Call<R>>, onSuccess: (R?) -> Unit,
        onError: (Response<R>) -> Unit, onFailure: (Throwable) -> Unit) {
        val retrofit = createRetrofit()
        val apiService = retrofit.create(AwsFunctionService::class.java)
        val call = callback.apply(apiService)
        enqueue(call, onSuccess, onError, onFailure)
    }

    fun getNearbyPlaces(location: UserLocation, onSuccess: (PlacesResponse?) -> Unit,
        onError: (Response<PlacesResponse>) -> Unit) {
        val request = NearbyPlacesRequest(location.latitude!!, location.longitude!!, 1000)
        invoke({ dd -> return@invoke dd.getNearbyPlaces(request) }, onSuccess, onError){}
    }

    fun describeLocation(location: String, onSuccess: (String?) -> Unit,
        onError: (Response<String>) -> Unit) {
        invoke({ dd -> return@invoke dd.describeLocation(location) }, onSuccess, onError){}
    }

    fun describeLocations(locations: List<String>, onSuccess: (String?) -> Unit,
        onError: (Response<String>) -> Unit) {
        invoke({ dd -> return@invoke dd.describeLocations(locations) }, onSuccess, onError){}
    }

    fun synthesizeText(request: String, onSuccess: (SynthesizeTextResponse?) -> Unit,
        onError: (Response<SynthesizeTextResponse>) -> Unit) {
        invoke({ dd -> return@invoke dd.synthesizeText(request) }, onSuccess, onError){}
    }

    fun recognize(request: String, onSuccess: (String?) -> Unit,
        onError: (Response<String>) -> Unit) {
        invoke({ dd -> return@invoke dd.recognize(request) }, onSuccess, onError){}
    }

    fun generateAudioDescriptionFromPlacesName(request: List<String>,
        onSuccess: (TextToSpeechResponse?) -> Unit, onError: (Response<TextToSpeechResponse>) -> Unit,
        onFailure: (Throwable) -> Unit) {
        invoke({ dd -> return@invoke dd.generateAudioDescriptionFromPlacesName(request) }, onSuccess, onError, onFailure)
    }

}