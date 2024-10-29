package com.jarnunes.udinetour.message

class ChatSessionInfo {

    private var senderName: String? = null
    private var senderUID: String? = null
    private var senderRoom: String? = null

    private var receiverName: String? = null
    private var receiverUID: String? = null
    private var receiverRoom: String? = null

    constructor()
    constructor(
        receiverUID: String?, receiverName: String?, senderUID: String?,
        senderName: String?
    ) {
        this.receiverUID = receiverUID
        this.receiverName = receiverName
        this.senderUID = senderUID
        this.senderName = senderName
    }

    fun getSenderName(): String? {
        return this.senderName
    }

    fun setSenderName(name: String?) {
        this.senderName = name
    }

    fun getSenderUID(): String? {
        return this.senderUID
    }

    fun setSenderUID(uid: String?) {
        this.senderUID = uid
    }

    fun getReceiverName(): String? {
        return this.receiverName
    }

    fun setReceiverName(name: String?) {
        this.receiverName = name
    }

    fun getReceiverUID(): String? {
        return this.receiverUID
    }

    fun setReceiverUID(uid: String?) {
        this.receiverUID = uid
    }

    fun getReceiverRoom(): String? {
        return this.receiverRoom
    }

    fun setReceiverRoom(room: String?) {
        this.receiverRoom = room
    }

    fun getSenderRoom(): String? {
        return this.senderRoom
    }

    fun setSenderRoom(room: String?) {
        this.senderRoom = room
    }

}