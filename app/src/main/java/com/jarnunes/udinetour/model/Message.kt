package com.jarnunes.udinetour.model

import java.io.Serializable

class Message : Serializable {


    var message: String? = null
    var sentId: String? = null
    var imagePath: String? = null

    constructor() {}

    constructor(messageIn: String?, senderIdIn: String?, imagePathIn: String?) {
        this.message = messageIn
        this.sentId = senderIdIn
        this.imagePath = imagePathIn
    }

}