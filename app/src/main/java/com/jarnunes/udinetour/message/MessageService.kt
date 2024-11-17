package com.jarnunes.udinetour.message

import android.annotation.SuppressLint
import com.jarnunes.udinetour.MainActivity
import com.jarnunes.udinetour.helper.DeviceHelper
import com.jarnunes.udinetour.helper.FileHelper
import com.jarnunes.udinetour.integrations.dto.Place
import com.jarnunes.udinetour.maps.location.LocationServiceBase
import java.io.File
import java.time.LocalDateTime

class MessageService(private val activity: MainActivity) : LocationServiceBase(activity) {
    private var messageList: ArrayList<Message> = ArrayList()
    private var fileHelper: FileHelper = FileHelper()
    private var deviceHelper: DeviceHelper = DeviceHelper()

    fun createUserTextMessage(message: String, beforeCreateMessageCallback: (ArrayList<Message>) -> Unit) {
        createTextMessage(message, SenderMessageType.USER, beforeCreateMessageCallback)
    }

    fun createSystemTextMessage(message: String, beforeCreateMessageCallback: (ArrayList<Message>) -> Unit) {
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
        writeMessages()
        beforeCreateMessageCallback(messageList)
    }

    private fun writeMessages(){
        fileHelper.writeMessages(messageList, activity.applicationContext)
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

    @SuppressLint("NewApi")
    fun createSystemWaitStartMessage() {
        val message = Message()
        message.messageType = MessageType.SYSTEM_WAIT_START
        message.sentId = getSentId(SenderMessageType.SYSTEM)
        message.reference = LocalDateTime.now()
        message.message = "Aguarde enquanto buscamos informações dos locais próximos..."
        messageList.add(message)
        writeMessages()
    }

    fun removeSystemWaitMessage(){
        messageList.removeIf { MessageType.SYSTEM_WAIT_START == it.messageType}
        writeMessages()
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
        writeMessages()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadMessages() {
        val storedMessageList = fileHelper.readMessages(activity.applicationContext)
        messageList.clear()
        messageList.addAll(storedMessageList)
        //todo: REMOVER
         // deleteAllMessages()

//        if (CollectionUtils.isEmpty(messageList)) {
//            var createdMapMessage = false
//
//            locationService.getCurrentLocation {
//                this.userLocation = it
//
//                if (!createdMapMessage) {
//                    //createMapMessage()
//                    //TODO: remover
//                    createSampleAudioMessage()
//
//                    this.activity.getMessageAdapter().notifyDataSetChanged()
//                    createdMapMessage = true
//                }
//            }
//        }
    }

    fun messageListCount(): Int {
        return messageList.size
    }

    fun empty(): Boolean{
        return messageList.isEmpty()
    }

    fun deleteAllMessages() {
        deleteFileMessages()
        messageList.clear()
        writeMessages()
    }

    private fun deleteFileMessages() {
        messageList
            .filter { msg -> msg.messageType.isAudio() || msg.messageType.isImage() }
            .filter { msg -> msg.resourcePath != null }
            .map { msg -> msg.resourcePath.toString() }
            .forEach { resource -> fileHelper.deleteFileByPath(activity, resource) }
    }

    fun getAllMessages(): ArrayList<Message> {
        return messageList
    }

    fun createMapMessage(places: ArrayList<Place>) {
        messageList.removeIf{msg -> msg.messageType == MessageType.MAP}

        val mapMessage = MapMessage()
        mapMessage.places = places
        mapMessage.sentId = getSentId(SenderMessageType.SYSTEM)
        mapMessage.setUserLocation(userLocation)
        messageList.add(mapMessage)
        writeMessages()
    }

    fun createAudioMessage(encodedAudioBase64: String) {
        val audio = fileHelper.createAudioFile(activity, encodedAudioBase64)
        val message = Message()
        message.resourcePath = audio.absolutePath
        message.sentId = getSentId(SenderMessageType.SYSTEM)
        message.messageType = MessageType.AUDIO
        message.setUserLocation(userLocation)
        messageList.add(message)
        writeMessages()
    }

}