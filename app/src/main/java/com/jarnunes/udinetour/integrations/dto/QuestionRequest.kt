package com.jarnunes.udinetour.integrations.dto

import com.jarnunes.udinetour.message.UserLocation

data class QuestionRequest(
    val question: String,
    val formatType: QuestionFormatType,
    val location: UserLocation
)
