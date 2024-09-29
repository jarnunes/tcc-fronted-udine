package com.jarnunes.udinetour.model

import java.io.Serializable

class Message : Serializable {


    var message: String? = null
    var sentId: String? = null

    constructor() {}

    constructor(messageIn: String?, senderIdIn: String?) {
        this.message = messageIn
        this.sentId = senderIdIn
    }

}