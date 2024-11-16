package com.jarnunes.udinetour.message

import com.jarnunes.udinetour.integrations.dto.Place

open class MapMessage : Message() {
    var places: ArrayList<Place> = ArrayList()

    init {
        this.messageType = MessageType.MAP
    }

}