package com.jarnunes.udinetour

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jarnunes.udinetour.adapter.MessageAdapter
import com.jarnunes.udinetour.databinding.ActivityMainBinding
import com.jarnunes.udinetour.maps.PlacesApiServiceImpl
import com.jarnunes.udinetour.maps.location.ActivityResultProvider
import com.jarnunes.udinetour.message.MessageService
import com.jarnunes.udinetour.recorder.AudioService

class MainActivity : AppCompatActivity(), ActivityResultProvider {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var audioService: AudioService
    private lateinit var messageService: MessageService
    private var currentImagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.chatToolbar)
        initialize()
        configureMessages()
        configureMainView()
        configureListenerForSendMessages()
        configureListenerForAudioRecorder()
        addWatcherToShowHideSendButton(binding.chatInputMessage, binding.chatSendMessageIcon)
    }

    private fun configureMessages() {
        this.messageAdapter =
            MessageAdapter(this, messageService.getAllMessages(), supportFragmentManager)
        this.messageAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun configureListenerForAudioRecorder() {
        binding.audioRecorder.setOnClickListener {
            audioService.record(
                afterStopRecordCallback = { audioFile ->
                    binding.audioRecorder.setImageResource(R.drawable.baseline_mic_24)

                    messageService.createUserAudioMessage(
                        audioFile = audioFile!!
                    )
                    messageAdapter.notifyDataSetChanged()
                    binding.chatRecycler.scrollToPosition(messageService.messageListCount() - 1)
                },
                afterStartRecordCallback = {
                    binding.audioRecorder.setImageResource(R.drawable.sharp_mic_off_24)
                }
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun configureListenerForSendMessages() {
        // add the message to database
        binding.chatSendMessageIcon.setOnClickListener {
            val message = binding.chatInputMessage.text.toString()

            messageService.createUserTextMessage(message) { messages ->
                messageService.createSystemTextMessage("SYSTEM_MESSAGE") {}

                PlacesApiServiceImpl(this).getNearbyPlaces { placesResult ->
                    // Manipule a lista `placesResult` aqui, por exemplo:
                    if (placesResult.isNotEmpty()) {
                        placesResult.forEach { place ->
                            val msg = StringBuilder()
                            msg.append("Name: ").append(place.name).append("\n")
                            msg.append("Vicinity: ").append(place.vicinity).append("\n")
                            msg.append("Latitude: ").append(place.geometry.location.lat)
                                .append("\n")
                            msg.append("Longitude: ").append(place.geometry.location.lng)
                                .append("\n")
                            messageService.createSystemTextMessage(msg.toString()) {}
                        }

                        messageAdapter.notifyDataSetChanged()
                        binding.chatRecycler.scrollToPosition(messages.size - 1)
                    } else {
                        //implementar registro de logs
                    }
                }

                currentImagePath = null
                messageAdapter.notifyDataSetChanged()

                binding.chatInputMessage.setText("")
                binding.chatRecycler.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun configureMainView() {
        binding.chatRecycler.layoutManager = LinearLayoutManager(this)
        binding.chatRecycler.adapter = messageAdapter
        binding.chatRecycler.scrollToPosition(messageService.messageListCount() - 1)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.chatRecycler.layoutManager = layoutManager
    }

    private fun initialize() {
        this.audioService = AudioService(this)
        this.messageService = MessageService(this)
        this.messageService.loadMessages()
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

    private fun getReceiverUID(): String {
        return getString(R.string.app_name) + "UID"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_bar_delete -> {
                val alert = AlertDialog.Builder(this)
                alert.setTitle("Deletar")
                alert.setMessage("Confirma exclusão das mensagens?")
                alert.setCancelable(false)
                alert.setNegativeButton(
                    "Não"
                ) { dialogInterface, i ->
                    dialogInterface.cancel()
                }

                alert.setPositiveButton(
                    "Sim"
                ) { dialogInterface, i ->
                    // Limpa todas as mensagens
                    messageService.deleteAllMessages()
                    messageAdapter.notifyDataSetChanged()
                }

                alert.create().show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun <I, O> getActivityResultLauncher(
        contract: ActivityResultContract<I, O>,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> {
        return registerForActivityResult(contract, callback)
    }

    override fun getAppContext(): Context {
        return this.applicationContext
    }

    override fun getMessageAdapter(): MessageAdapter {
        return this.messageAdapter
    }
}