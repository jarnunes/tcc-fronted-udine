package com.jarnunes.udinetour.message

import java.io.Serializable
import java.time.LocalDateTime

open class Message : Serializable {
    var reference: LocalDateTime? = null
    var message: String? = null
    var sentId: String? = null
    var messageType: MessageType = MessageType.TEXT
    var resourcePath: String? = null
    var userLocation: UserLocation? = null

    constructor()

    constructor(messageIn: String?, senderIdIn: String?, resourcePathIn: String?) {
        this.message = messageIn
        this.sentId = senderIdIn
        this.resourcePath = resourcePathIn
        this.userLocation = UserLocation()
    }

}