package com.jarnunes.udinetour

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jarnunes.udinetour.adapter.MessageAdapter
import com.jarnunes.udinetour.helper.DeviceHelper
import com.jarnunes.udinetour.helper.FileHelper
import com.jarnunes.udinetour.model.Message

class MainActivity : AppCompatActivity() {

    private lateinit var messageText: EditText
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var deviceHelper: DeviceHelper

    private var fileHelper = FileHelper()
    private var receiverRoom: String? = null
    private var senderRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        deviceHelper = DeviceHelper()
        val senderName = "DeviceName"
        val senderUid = deviceHelper.getDeviceId(applicationContext)

        val receiverName = getString(R.string.app_name)
        val receiverUid = getReceiverUID()

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        chatRecyclerView = findViewById(R.id.chatRecycler)
        messageText = findViewById(R.id.chatInputMessage)
        sendButton = findViewById(R.id.chatSendMessageIcon)

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        // logic for add data to recycler view

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        val storedMessageList = fileHelper.readMessages(applicationContext)
        messageList.clear()
        messageList.addAll(storedMessageList)


        // add the mesage to database
        sendButton.setOnClickListener {
            val message = messageText.text.toString()
            val messageObject = Message(message, senderUid)

            messageList.add(messageObject)
            messageList.add(createSystemMessage())
            messageText.setText("")
            fileHelper.writeMessages(messageList, applicationContext)
            messageAdapter.notifyDataSetChanged()
        }

    }

    private fun createSystemMessage(): Message {
        return Message("This is a system message", getReceiverUID())
    }

    private fun getReceiverUID(): String {
        return getString(R.string.app_name) + "UID"
    }
}