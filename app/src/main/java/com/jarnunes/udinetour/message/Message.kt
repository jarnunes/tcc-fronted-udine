package com.jarnunes.udinetour.message

import java.io.Serializable
import java.time.LocalDateTime

class Message : Serializable {
    var reference: LocalDateTime? = null
    var message: String? = null
    var sentId: String? = null
    var messageType: MessageType = MessageType.TEXT
    var resourcePath: String? = null
    private lateinit var userLocation: UserLocation

    constructor() {}

    constructor(messageIn: String?, senderIdIn: String?, resourcePathIn: String?) {
        this.message = messageIn
        this.sentId = senderIdIn
        this.resourcePath = resourcePathIn
        this.userLocation = UserLocation()
    }

    fun setUserLocation(userLocation: UserLocation) {
        this.userLocation = userLocation
    }

    fun getUserLocation(): UserLocation {
        return this.userLocation
    }

}