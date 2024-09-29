package com.jarnunes.udinetour.helper

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

class DeviceHelper {

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}