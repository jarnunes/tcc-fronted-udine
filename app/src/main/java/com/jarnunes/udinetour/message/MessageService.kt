package com.jarnunes.udinetour.message

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import com.jarnunes.udinetour.MainActivity
import com.jarnunes.udinetour.helper.DeviceHelper
import com.jarnunes.udinetour.helper.FileHelper
import com.jarnunes.udinetour.integrations.dto.Place
import java.io.File
import java.time.LocalDateTime

class MessageService(private val activity: MainActivity) {
    private var messageList: ArrayList<Message> = ArrayList()
    private var fileHelper: FileHelper = FileHelper()
    private var deviceHelper: DeviceHelper = DeviceHelper()

    fun createUserTextMessage(message: String, userLocation: UserLocation) {
        createTextMessage(message, SenderMessageType.USER, userLocation)
    }

    fun createSystemTextMessage(message: String, userLocation: UserLocation) {
        createTextMessage(message, SenderMessageType.SYSTEM, userLocation)
    }

    private fun createTextMessage(message: String, senderType: SenderMessageType,
        userLocation: UserLocation) {
        val messageObject = Message()
        messageObject.message = message
        messageObject.messageType = MessageType.TEXT
        messageObject.sentId = getSentId(senderType)
        messageObject.userLocation = userLocation
        messageList.add(messageObject)
        writeMessages()
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

    fun createUserAudioMessage(audioFile: File, userLocation: UserLocation) {
        createAudioMessage(audioFile, SenderMessageType.USER, userLocation)
    }

    @SuppressLint("NewApi")
    fun createSystemWaitStartMessage(@StringRes resId:  Int) {
        val message = Message()
        message.messageType = MessageType.SYSTEM_WAIT_START
        message.sentId = getSentId(SenderMessageType.SYSTEM)
        message.reference = LocalDateTime.now()
        message.message = activity.getString(resId)
        messageList.add(message)
        writeMessages()
    }

    fun removeSystemWaitMessage(){
        messageList.removeIf { MessageType.SYSTEM_WAIT_START == it.messageType}
        writeMessages()
    }

    fun createSystemAudioMessage(audioFile: File, userLocation: UserLocation) {
        createAudioMessage(audioFile, SenderMessageType.SYSTEM, userLocation)
    }

    private fun createAudioMessage(audioFile: File, senderType: SenderMessageType, userLocation: UserLocation) {
        val audioMessage = Message()
        audioMessage.userLocation = userLocation
        audioMessage.resourcePath = audioFile.absolutePath
        audioMessage.sentId = getSentId(senderType)
        audioMessage.messageType = MessageType.AUDIO
        messageList.add(audioMessage)
        writeMessages()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadMessages() {
        val storedMessageList = fileHelper.readMessages(activity.applicationContext)
        removeSystemMessages(storedMessageList)
        messageList.clear()
        messageList.addAll(storedMessageList)
    }

    private fun removeSystemMessages(storedMessageList: ArrayList<Message>) {
        storedMessageList.removeIf {
            it.messageType == MessageType.SYSTEM_WAIT_START
                    || it.messageType == MessageType.SYSTEM_WAIT
        }
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

    fun createMapMessage(location: UserLocation, places: ArrayList<Place>) {
        messageList.removeIf{msg -> msg.messageType == MessageType.MAP}

        val mapMessage = MapMessage()
        mapMessage.userLocation = location
        mapMessage.places = places
        mapMessage.sentId = getSentId(SenderMessageType.SYSTEM)
        messageList.add(mapMessage)
        writeMessages()
    }

    fun getMapMessageLocationsId(): List<String> {
        return messageList.filter { it.messageType == MessageType.MAP }
            .map { it as MapMessage }
            .flatMap { it.places }
            .map { it.id }.toList()
    }

    fun createAudioMessage(encodedAudioBase64: String) {
        val audio = fileHelper.createAudioFile(activity, encodedAudioBase64)
        val message = Message()
        message.resourcePath = audio.absolutePath
        message.sentId = getSentId(SenderMessageType.SYSTEM)
        message.messageType = MessageType.AUDIO
        messageList.add(message)
        writeMessages()
    }

}