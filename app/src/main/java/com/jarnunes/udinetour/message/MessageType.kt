package com.jarnunes.udinetour.message

import java.io.Serializable

enum class MessageType : Serializable {

    TEXT,
    IMAGE,
    AUDIO,
    MAP,
    LOCATION
}