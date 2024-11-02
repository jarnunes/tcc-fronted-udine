package com.jarnunes.udinetour.message

import com.google.android.gms.common.util.CollectionUtils
import com.jarnunes.udinetour.MainActivity
import com.jarnunes.udinetour.helper.DeviceHelper
import com.jarnunes.udinetour.helper.FileHelper
import com.jarnunes.udinetour.maps.location.LocationServiceBase
import java.io.File

class MessageService(private val activity: MainActivity) : LocationServiceBase(activity) {
    private var messageList: ArrayList<Message> = ArrayList()
    private var fileHelper: FileHelper = FileHelper()
    private var deviceHelper: DeviceHelper = DeviceHelper()

    fun createUserTextMessage(
        message: String,
        beforeCreateMessageCallback: (ArrayList<Message>) -> Unit
    ) {
        createTextMessage(message, SenderMessageType.USER, beforeCreateMessageCallback)
    }

    fun createSystemTextMessage(
        message: String, beforeCreateMessageCallback: (ArrayList<Message>) -> Unit
    ) {
        createTextMessage(message, SenderMessageType.SYSTEM, beforeCreateMessageCallback)
    }

    private fun createTextMessage(
        message: String,
        senderType: SenderMessageType,
        beforeCreateMessageCallback: (ArrayList<Message>) -> Unit
    ) {
        val messageObject = Message()
        messageObject.message = message
        messageObject.messageType = MessageType.TEXT
        messageObject.sentId = getSentId(senderType)
        messageObject.setUserLocation(userLocation)
        messageList.add(messageObject)
        fileHelper.writeMessages(messageList, activity.applicationContext)

        beforeCreateMessageCallback(messageList)
    }

    private fun getSentId(senderMessageType: SenderMessageType): String {
        return if (SenderMessageType.USER == senderMessageType) {
            deviceHelper.getUserDeviceId(activity.applicationContext)
        } else {
            deviceHelper.getSystemDeviceId()
        }
    }

    fun createUserAudioMessage(audioFile: File) {
        createAudioMessage(audioFile, SenderMessageType.USER)
    }

    fun createSystemAudioMessage(userLocation: UserLocation, audioFile: File) {
        createAudioMessage(audioFile, SenderMessageType.SYSTEM)
    }

    private fun createAudioMessage(audioFile: File, senderType: SenderMessageType) {
        val audioMessage = Message()
        audioMessage.setUserLocation(userLocation)
        audioMessage.resourcePath = audioFile.absolutePath
        audioMessage.sentId = getSentId(senderType)
        audioMessage.messageType = MessageType.AUDIO
        messageList.add(audioMessage)
        fileHelper.writeMessages(messageList, activity.applicationContext)
    }

    fun loadMessages() {
        val storedMessageList = fileHelper.readMessages(activity.applicationContext)
        messageList.clear()
        messageList.addAll(storedMessageList)
        deleteAllMessages()

        if (CollectionUtils.isEmpty(messageList)) {
            var createdMapMessage = false

            locationService.getCurrentLocation {
                this.userLocation = it

                if (!createdMapMessage) {
                    createMapMessage()
                    this.activity.getMessageAdapter().notifyDataSetChanged()
                    createdMapMessage = true
                }
            }
        }
    }

    fun messageListCount(): Int {
        return messageList.size
    }

    fun deleteAllMessages() {
        messageList.clear()
        fileHelper.writeMessages(messageList, activity.applicationContext)
    }

    fun getAllMessages(): ArrayList<Message> {
        return messageList
    }

    fun createMapMessage() {
        val mapMessage = Message()
        mapMessage.messageType = MessageType.MAP
        mapMessage.sentId = getSentId(SenderMessageType.SYSTEM)
        mapMessage.setUserLocation(userLocation)
        messageList.add(mapMessage)
        fileHelper.writeMessages(messageList, activity.applicationContext)
    }

    fun getMapMessage(): Message {
        return messageList.first { msg -> MessageType.MAP == msg.messageType }
    }
}