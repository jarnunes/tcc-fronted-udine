package com.jarnunes.udinetour.integrations.dto

data class QuestionRequest(
    val question: String,
    val formatType: QuestionFormatType,
    val placesId: List<String>
)
