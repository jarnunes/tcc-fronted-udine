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
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jarnunes.udinetour.adapter.MessageAdapter
import com.jarnunes.udinetour.commons.ExceptionUtils
import com.jarnunes.udinetour.commons.ILog
import com.jarnunes.udinetour.databinding.ActivityMainBinding
import com.jarnunes.udinetour.helper.FileHelper
import com.jarnunes.udinetour.integrations.IntegrationService
import com.jarnunes.udinetour.integrations.dto.QuestionFormatType.AUDIO
import com.jarnunes.udinetour.integrations.dto.QuestionFormatType.TEXT
import com.jarnunes.udinetour.integrations.dto.QuestionRequest
import com.jarnunes.udinetour.maps.location.ActivityResultProvider
import com.jarnunes.udinetour.maps.location.UserLocationService
import com.jarnunes.udinetour.message.MessageService
import com.jarnunes.udinetour.recorder.AudioService

class MainActivity : AppCompatActivity(), ActivityResultProvider {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var audioService: AudioService
    private lateinit var messageService: MessageService
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
    }

    private fun configureMessageAdapter() {
        this.messageAdapter =
            MessageAdapter(this, messageService.getAllMessages(), supportFragmentManager)
        notifyDataSetChanged()
    }

    private fun configureInitMessages() {
        var locationFetched = false
        var descriptionFetched = false
        val places = ArrayList<String>()

        if (messageService.empty()) {
            addSystemWaitProcess(R.string.system_msg_search_nearby_places)
            notifyDataSetChanged()

            UserLocationService.getCurrentLocation{ location ->
                if (!locationFetched) {
                    integrationService.getNearbyPlaces(location,
                        onSuccess = { placesResponse ->
                            locationFetched = true

                            if (placesResponse != null) {
                                messageService.createMapMessage(location, placesResponse.results)
                                removeSystemWaitProcess()
                                notifyDataSetChanged()
                                placesResponse.results.map { it.name }.forEach { places.add(it) }

                            }
                        },
                        onError = {}
                    )
                }

//                if (locationFetched && !descriptionFetched) {
//                    integrationService.generateAudioDescriptionFromPlacesName(places,
//                        onSuccess = { response ->
//                            descriptionFetched = true
//                            messageService.createAudioMessage(response?.audioContent!!)
//                            removeSystemWaitProcess()
//                            notifyDataSetChanged()
//                            ILog.i(ILog.INTEGRATION_SERVICE, response.toString())
//                        },
//                        onError = { response ->
//                            ILog.e(ILog.INTEGRATION_SERVICE, "Erro ao obter a descrição dos locais")
//                        },
//                        onFailure = { throwable ->
//                            showErrorDialog(
//                                "generateAudioDescriptionFromPlacesName.onFailure",
//                                throwable
//                            )
//                            removeSystemWaitProcess()
//                            ILog.e(ILog.INTEGRATION_SERVICE, throwable)
//                        }
//                    )
//                }
            }
        }
    }

    private fun configureListenerForAudioRecorder() {
        binding.audioRecorder.setOnClickListener {
            audioService.record(
                afterStopRecordCallback = { audioFile ->
                    binding.audioRecorder.setImageResource(R.drawable.baseline_mic_24)
                    messageService.createUserAudioMessage(audioFile!!)

                    addSystemWaitProcess(R.string.system_msg_process_text_message)
                    val locationsID = messageService.getMapMessageLocationsId()
                    val encodedAudio = FileHelper().encodeFileToBase64(audioFile)
                    val byteArray = audioFile.readBytes()
                    val request = QuestionRequest(encodedAudio, AUDIO, locationsID)
                    integrationService.answerQuestion(request,
                        onSuccess = {questionResponse ->
                            ILog.i(ILog.INTEGRATION_SERVICE, "Resposta do serviço de audio ")
                            val response = questionResponse?.response
                            messageService.createAudioMessage(response!!)
                            removeSystemWaitProcess()
                            createDialog(R.string.dialog_info_title, "Resposta do serviço de audio com sucesso.")
                        },
                        onError = {questionResponse ->
                            ILog.i(ILog.INTEGRATION_SERVICE, "Resposta do serviço de audio com erro ")
                            removeSystemWaitProcess()
                            createDialog(R.string.dialog_info_title, "Resposta do serviço de audio com erro. ${questionResponse.message()}")
                        },
                        onFailure = {exception ->
                            ILog.i(ILog.INTEGRATION_SERVICE, "Resposta do serviço de audio com exceção ")
                            showErrorDialog("configureListenerForSendMessages", exception)
                            removeSystemWaitProcess()
                        })

                    notifyDataSetChanged()
                    scrollToBottom()
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
                val message = binding.chatInputMessage.text.toString()

                if (message.isNotEmpty()) {
                    messageService.createUserTextMessage(message) {}
                    addSystemWaitProcess(R.string.system_msg_process_text_message)

                    val locationsID = messageService.getMapMessageLocationsId()
                    val questionRequest = QuestionRequest(message, TEXT, locationsID)
                    integrationService.answerQuestion(questionRequest,
                        onSuccess = {questionResponse ->
                            val response = questionResponse?.response
                            messageService.createSystemTextMessage(response!!){}
                            removeSystemWaitProcess()
                        },
                        onError = {questionResponse ->

                            removeSystemWaitProcess()
                        },
                        onFailure = {exception ->
                            showErrorDialog("configureListenerForSendMessages", exception)
                            removeSystemWaitProcess()
                        })

                    notifyDataSetChanged()
                    binding.chatInputMessage.setText("")
                    notifyDataSetChanged()
                    scrollToBottom()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyDataSetChanged() {
        messageAdapter.notifyDataSetChanged()
    }

    private fun scrollToBottom(){
        binding.chatRecycler.scrollToPosition(messageService.messageListCount() - 1)
    }

    private fun addSystemWaitProcess(@StringRes resId:  Int) {
        messageService.createSystemWaitStartMessage(resId)
        binding.chatSendMessageIcon.setImageResource(R.drawable.baseline_disabled_send_24)
        binding.chatSendMessageIcon.isEnabled = false
        notifyDataSetChanged()
    }

    private fun removeSystemWaitProcess() {
        messageService.removeSystemWaitMessage()
        binding.chatSendMessageIcon.setImageResource(R.drawable.baseline_send_24)
        binding.chatSendMessageIcon.isEnabled = true
        notifyDataSetChanged()
    }

    private fun configureMainView() {
        binding.chatRecycler.layoutManager = LinearLayoutManager(this)
        binding.chatRecycler.adapter = messageAdapter
        scrollToBottom()
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.chatRecycler.layoutManager = layoutManager
    }

    private fun initialize() {
        UserLocationService.initialize(this)
        this.audioService = AudioService(this)
        this.messageService = MessageService(this)
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
                        notifyDataSetChanged()
                        configureInitMessages()
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

    private fun showErrorDialog(serviceName: String, exception: Throwable) {
        val rootCause = ExceptionUtils.getRootCause(exception)
        val exceptionMessage = StringBuilder()
        exceptionMessage.append("Ocorreu um erro na execução do serviço: ").append(serviceName)
        exceptionMessage.append("\n\n")
        exceptionMessage.append("Detalhes: ").append("\n").append(rootCause)
        createDialog(R.string.dialog_error_title, exceptionMessage.toString())
    }

    private fun createDialog(@StringRes title: Int, message: String) {
        val errorTextView = TextView(this)
        errorTextView.text = message
        errorTextView.setPadding(50, 50, 50, 50)
        errorTextView.movementMethod = android.text.method.ScrollingMovementMethod()
        errorTextView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)

        val alertDialog = AlertDialog.Builder(this)
            .setTitle(getString(title))
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

}