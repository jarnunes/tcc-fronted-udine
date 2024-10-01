package com.jarnunes.udinetour

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jarnunes.udinetour.adapter.MessageAdapter
import com.jarnunes.udinetour.databinding.ActivityMainBinding
import com.jarnunes.udinetour.helper.DeviceHelper
import com.jarnunes.udinetour.helper.FileHelper
import com.jarnunes.udinetour.model.ChatSessionInfo
import com.jarnunes.udinetour.model.Message

class MainActivity : AppCompatActivity() {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var deviceHelper: DeviceHelper
    private lateinit var imageURI: Uri
    private lateinit var binding: ActivityMainBinding
    private lateinit var chatSessionInfo: ChatSessionInfo

    private var fileHelper = FileHelper()
    private var currentImagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.chatToolbar)

        initializeVariablesFromViewId()
        createChatSessionInfo()

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        // logic for add data to recycler view
        binding.chatRecycler.layoutManager = LinearLayoutManager(this)
        binding.chatRecycler.adapter = messageAdapter

        val storedMessageList = fileHelper.readMessages(applicationContext)
        messageList.clear()
        messageList.addAll(storedMessageList)

        // add the message to database
        binding.chatSendMessageIcon.setOnClickListener {
            val message = binding.chatInputMessage.text.toString()

            if (currentImagePath != null) {
                val messageObject = Message(null, chatSessionInfo.getSenderUID(), currentImagePath)
                messageList.add(messageObject)
            }

            val messageObject = Message(message, chatSessionInfo.getSenderUID(), null)
            messageList.add(messageObject)
            messageList.add(createSystemMessage())
            binding.chatInputMessage.setText("")
            fileHelper.writeMessages(messageList, applicationContext)
            currentImagePath = null
            messageAdapter.notifyDataSetChanged()
        }

        setChatImageEventListener()
        addWatcherToShowHideSendButton(binding.chatInputMessage, binding.chatSendMessageIcon)
    }

    private fun initializeVariablesFromViewId(){
        this.imageURI = fileHelper.createImageURI(this)
    }

    private fun createChatSessionInfo() {
        this.deviceHelper = DeviceHelper()
        this.chatSessionInfo = ChatSessionInfo()
        this.chatSessionInfo.setSenderName("DeviceName")
        this.chatSessionInfo.setSenderUID(deviceHelper.getDeviceId(applicationContext))
        this.chatSessionInfo.setReceiverName(getString(R.string.app_name))
        this.chatSessionInfo.setReceiverUID(getReceiverUID())
        this.chatSessionInfo.setSenderRoom(chatSessionInfo.getReceiverUID() + chatSessionInfo.getSenderUID())
        this.chatSessionInfo.setReceiverRoom(chatSessionInfo.getSenderUID() + chatSessionInfo.getReceiverUID())
    }

    private fun setChatImageEventListener() {
        // When image was captured
        val contract = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            currentImagePath = imageURI.toString()
        }

        // When button was clicked
        this.binding.chatCam.setOnClickListener {
            contract.launch(imageURI)
        }
    }

    private fun addWatcherToShowHideSendButton(messageText: EditText, sendButton: ImageView) {
        messageText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) {
                    sendButton.visibility = View.GONE
                } else {
                    sendButton.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun createSystemMessage(): Message {
        return Message("This is a system message", getReceiverUID(), null)
    }

    private fun getReceiverUID(): String {
        return getString(R.string.app_name) + "UID"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_bar_delete -> {
                val alert = AlertDialog.Builder(this)
                alert.setTitle("Deletar")
                alert.setMessage("Confirma exclusão das mensagens?")
                alert.setCancelable(false)
                alert.setNegativeButton(
                    "Não",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        dialogInterface.cancel()
                    })

                alert.setPositiveButton(
                    "Sim",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        // Limpa todas as mensagens
                        messageList.clear()
                        messageAdapter.notifyDataSetChanged()

                        // Limpa o armazenamento de mensagens (caso esteja salvando)
                        fileHelper.writeMessages(messageList, applicationContext)
                    })

                alert.create().show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


}