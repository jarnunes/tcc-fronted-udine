package com.jarnunes.udinetour

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jarnunes.udinetour.adapter.MessageAdapter
import com.jarnunes.udinetour.commons.ExceptionUtils
import com.jarnunes.udinetour.commons.ILog
import com.jarnunes.udinetour.databinding.ActivityMainBinding
import com.jarnunes.udinetour.integrations.IntegrationService
import com.jarnunes.udinetour.maps.location.ActivityResultProvider
import com.jarnunes.udinetour.maps.location.UserLocationService
import com.jarnunes.udinetour.message.MessageService
import com.jarnunes.udinetour.recorder.AudioService

class MainActivity : AppCompatActivity(), ActivityResultProvider {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var audioService: AudioService
    private lateinit var messageService: MessageService
    private lateinit var locationService: UserLocationService
    private lateinit var integrationService: IntegrationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.chatToolbar)
        initialize()
        configureMessageAdapter()
        configureInitMessages()
        configureMainView()
        configureListenerForSendMessages()
        configureListenerForAudioRecorder()
        //addWatcherToShowHideSendButton(binding.chatInputMessage, binding.chatSendMessageIcon)
    }

    private fun configureMessageAdapter(){
        this.messageAdapter =
            MessageAdapter(this, messageService.getAllMessages(), supportFragmentManager)
        notifyDataSetChanged()
    }

    private fun configureInitMessages() {
        var locationFetched = false
        var descriptionFetched = false
        val places = ArrayList<String>()

        if(messageService.empty()){
            addSystemWaitProcess()
            locationService.getCurrentLocation { location ->
                if(!locationFetched) {
                    integrationService.getNearbyPlaces(location,
                        onSuccess = { placesResponse ->
                            locationFetched = true

                            if (placesResponse != null) {
                                messageService.createMapMessage(placesResponse.results)
                                notifyDataSetChanged()
                                placesResponse.results.map { it.name }.forEach { places.add(it) }
                            }
                        },
                        onError = {}
                    )
                }

                if(locationFetched && !descriptionFetched){
                    integrationService.generateAudioDescriptionFromPlacesName(places,
                        onSuccess = { response ->
                            descriptionFetched = true
                            messageService.createAudioMessage(response?.audioContent!!)
                            notifyDataSetChanged()
                            removeSystemWaitProcess()
                            ILog.i(ILog.INTEGRATION_SERVICE, response.toString())
                        },
                        onError = { response ->
                            ILog.e(ILog.INTEGRATION_SERVICE, "Erro ao obter a descrição dos locais")
                        },
                        onFailure = { throwable ->
                            ILog.e(ILog.INTEGRATION_SERVICE, throwable)
                        }
                    )
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun configureListenerForAudioRecorder() {
        binding.audioRecorder.setOnClickListener {
            audioService.record(
                afterStopRecordCallback = { audioFile ->
                    binding.audioRecorder.setImageResource(R.drawable.baseline_mic_24)
                    messageService.createUserAudioMessage(audioFile!!)
                    messageAdapter.notifyDataSetChanged()
                    binding.chatRecycler.scrollToPosition(messageService.messageListCount() - 1)
                },
                afterStartRecordCallback = {
                    binding.audioRecorder.setImageResource(R.drawable.sharp_mic_off_24)
                }
            )
        }
    }

    private fun configureListenerForSendMessages() {
        binding.chatSendMessageIcon.setOnClickListener {
            executeOnTryCatch("configureListenerForSendMessages") {
                addSystemWaitProcess()
                val message = binding.chatInputMessage.text.toString()

                messageService.createUserTextMessage(message) { messages ->
                    notifyDataSetChanged()
                    binding.chatInputMessage.setText("")
                    binding.chatRecycler.scrollToPosition(messages.size - 1)
                    removeSystemWaitProcess()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyDataSetChanged(){
        messageAdapter.notifyDataSetChanged()
    }

    private fun addSystemWaitProcess(){
        messageService.createSystemWaitStartMessage()
        binding.chatSendMessageIcon.setImageResource(R.drawable.baseline_disabled_send_24)
        binding.chatSendMessageIcon.isEnabled = false
        notifyDataSetChanged()
    }

    private fun removeSystemWaitProcess(){
        messageService.removeSystemWaitMessage()
        binding.chatSendMessageIcon.setImageResource(R.drawable.baseline_send_24)
        binding.chatSendMessageIcon.isEnabled = true
        notifyDataSetChanged()
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
        this.locationService = UserLocationService(this)
        this.integrationService = IntegrationService(this)

        this.messageService.loadMessages()
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
                alert.setTitle(getString(R.string.dialog_delete_title))
                alert.setMessage(getString(R.string.dialog_delete_message))
                alert.setCancelable(false)
                alert.setNegativeButton(
                    getString(R.string.dialog_confirmation_no)
                ) { dialogInterface, _ ->
                    dialogInterface.cancel()
                }

                alert.setPositiveButton(
                    getString(R.string.dialog_confirmation_yes)
                ) { _, _ ->

                    executeOnTryCatch("onOptionsItemSelected") {
                        messageService.deleteAllMessages()
                        configureInitMessages()
                        messageAdapter.notifyDataSetChanged()
                    }
                }

                alert.create().show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun executeOnTryCatch(serviceName: String, callback: () -> Unit) {
        try {
            callback()
        } catch (exception: Exception) {
            Log.e("ExecuteOnTryCatch-$serviceName", exception.message.toString())
            showErrorDialog(serviceName, exception)
        }
    }

    private fun showErrorDialog(serviceName: String, exception: java.lang.Exception) {
        val rootCause = ExceptionUtils.getRootCause(exception)
        val exceptionMessage = StringBuilder()
        exceptionMessage.append("Ocorreu um erro na execução do serviço: ").append(serviceName)
        exceptionMessage.append("\n\n")
        exceptionMessage.append("Detalhes: ").append("\n").append(rootCause)

        val errorTextView = TextView(this)
        errorTextView.text = exceptionMessage
        errorTextView.setPadding(50, 50, 50, 50)
        errorTextView.movementMethod = android.text.method.ScrollingMovementMethod()
        errorTextView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)

        val alertDialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_error_title))
            .setView(errorTextView)
            .setPositiveButton(getString(R.string.dialog_confirmation_ok)) { dialog, _ -> dialog.dismiss() }
            .create()
        alertDialog.show()
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