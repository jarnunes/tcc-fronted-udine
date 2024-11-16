package com.jarnunes.udinetour.message

import java.io.Serializable

enum class MessageType : Serializable {

    TEXT,
    IMAGE,
    AUDIO,
    MAP,
    LOCATION,
    SYSTEM_WAIT,
    SYSTEM_ALERT;

    fun isText(): Boolean {
        return this == TEXT
    }

    fun isImage(): Boolean {
        return this == IMAGE
    }

    fun isAudio(): Boolean {
        return this == AUDIO
    }

    fun isMap(): Boolean {
        return this == MAP
    }

    fun isLocation(): Boolean {
        return this == LOCATION
    }

}