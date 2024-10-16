package com.jarnunes.udinetour.model

import java.io.Serializable

class Message : Serializable {

    var message: String? = null
    var sentId: String? = null
    var messageType: MessageType? = null
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