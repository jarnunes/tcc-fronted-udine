package com.jarnunes.udinetour.model

class User {
    var uid: String? = null
    var name: String? = null

    constructor() {}

    constructor(uidIn: String?, nameIn: String?) {
        this.uid = uidIn
        this.name = nameIn
    }

}