package com.jarnunes.udinetour

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        if(messageService.empty()) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    addSystemWaitProcess(R.string.system_msg_search_nearby_places)
                    val location = UserLocationService.getUserLocation()

                    val placesResponse = integrationService.getNearbyPlacesAsync(location)
                    messageService.createMapMessage(location, placesResponse.results)

                    val locationsDescription = integrationService
                        .generateAudioDescriptionFromPlacesNameAsync(placesResponse.results.map { it.name }
                            .toList())
                    messageService.createAudioMessage(locationsDescription.audioContent)
                    notifyDataSetChanged()
                } catch (e: Exception) {
                    showErrorDialog("Obter localização, locais próximos e gerar descrição.", e)
                }
                removeSystemWaitProcess()
            }
            notifyDataSetChanged()
        }
    }

    private fun configureListenerForAudioRecorder() {
        binding.audioRecorder.setOnClickListener {
            audioService.record(
                afterStopRecordCallback = { audioFile ->
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val userLocation = UserLocationService.getUserLocation()
                            binding.audioRecorder.setImageResource(R.drawable.baseline_mic_24)
                            messageService.createUserAudioMessage(audioFile!!, userLocation)

                            addSystemWaitProcess(R.string.system_msg_process_text_message)
                            val locationsID = messageService.getMapMessageLocationsId()
                            val encodedAudio = FileHelper().encodeFileToBase64(audioFile)
                            val request = QuestionRequest(encodedAudio, AUDIO, locationsID)

                            val response = integrationService.answerQuestionAsync(request)
                            messageService.createAudioMessage(response.response)
                        } catch (e: Exception) {
                            ILog.e(ILog.INTEGRATION_SERVICE, e)
                            showErrorDialog("Responder pergunta via audio.", e)
                        }
                        removeSystemWaitProcess()
                        notifyDataSetChanged()
                        scrollToBottom()
                    }
                },
                afterStartRecordCallback = {
                    binding.audioRecorder.setImageResource(R.drawable.sharp_mic_off_24)
                }
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão foi concedida. Pode chamar o método record novamente se necessário.
            } else {
                // Permissão negada. Notifique o usuário ou desabilite o recurso.
                AlertDialog.Builder(this)
                    .setTitle("Permissão necessária")
                    .setMessage("Para gravar áudios, é necessário permitir o uso do microfone.")
                    .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
    }

    private fun configureListenerForSendMessages() {
        binding.chatSendMessageIcon.setOnClickListener {
            val message = binding.chatInputMessage.text.toString()

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    if (message.isNotEmpty()) {
                        binding.chatInputMessage.setText("")

                        val userLocation = UserLocationService.getUserLocation()
                        messageService.createUserTextMessage(message, userLocation)
                        addSystemWaitProcess(R.string.system_msg_process_text_message)
                        notifyDataSetChanged()

                        val locationsID = messageService.getMapMessageLocationsId()
                        val questionRequest = QuestionRequest(message, TEXT, locationsID)
                        val answerResponse = integrationService.answerQuestionAsync(questionRequest)
                        val response = answerResponse.response
                        messageService.createSystemTextMessage(response, userLocation)
                    }
                } catch (e: Exception) {
                    showErrorDialog("Responser pergunta de texto", e)
                }

                removeSystemWaitProcess()
                notifyDataSetChanged()
                scrollToBottom()
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