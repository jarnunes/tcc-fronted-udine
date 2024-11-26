package com.jarnunes.udinetour.integrations.dto

data class QuestionResponse(
    val response: String,
    val formatType: QuestionFormatType,
    val placePhotos: List<PlacePhoto> = emptyList(),
    val success: Boolean = true
)
