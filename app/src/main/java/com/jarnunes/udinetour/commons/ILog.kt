package com.jarnunes.udinetour.commons

import android.util.Log
import com.jarnunes.udinetour.helper.FileHelper
import com.jarnunes.udinetour.integrations.IntegrationService
import com.jarnunes.udinetour.maps.MapService

enum class ILog(tagName: String) {

    MAP_SERVICE(MapService::class.simpleName.toString()),
    FILE_HELPER(FileHelper::class.simpleName.toString()),
    INTEGRATION_SERVICE(IntegrationService::class.simpleName.toString());

    companion object {
        fun fileHelper(): String {
            return FILE_HELPER.toString()
        }

        fun e(tag: ILog, message: String) {
            Log.e(tag.name, message)
        }

        fun e(tag: ILog, exception: Throwable) {
            Log.e(tag.name, exception.message.toString())
        }

        fun i(tag: ILog, message: String) {
            Log.i(tag.name, message)
        }
    }
}