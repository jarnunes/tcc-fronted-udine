package com.jarnunes.udinetour.maps.location

import android.content.Context
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import com.jarnunes.udinetour.adapter.MessageAdapter

interface ActivityResultProvider {
    fun <I, O> getActivityResultLauncher(
        contract: ActivityResultContract<I, O>,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I>

    fun getAppContext(): Context

    fun getMessageAdapter(): MessageAdapter
}