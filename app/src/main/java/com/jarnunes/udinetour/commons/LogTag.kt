package com.jarnunes.udinetour.commons

import com.jarnunes.udinetour.helper.FileHelper

enum class LogTag(tagName: String) {

    FILE_HELPER(FileHelper::class.simpleName.toString());

    companion object {
        fun fileHelper(): String {
            return FILE_HELPER.toString()
        }
    }
}