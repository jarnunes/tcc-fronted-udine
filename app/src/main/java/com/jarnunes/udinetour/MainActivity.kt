package com.jarnunes.udinetour

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
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
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.jarnunes.udinetour.adapter.MessageAdapter
import com.jarnunes.udinetour.databinding.ActivityMainBinding
import com.jarnunes.udinetour.helper.DeviceHelper
import com.jarnunes.udinetour.helper.FileHelper
import com.jarnunes.udinetour.model.ChatSessionInfo
import com.jarnunes.udinetour.model.Message
import com.jarnunes.udinetour.model.MessageType
import com.jarnunes.udinetour.model.UserLocation
import com.jarnunes.udinetour.recorder.AndroidAudioPlayer
import com.jarnunes.udinetour.recorder.AndroidAudioRecorder
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val recorder by lazy {
        AndroidAudioRecorder(applicationContext)
    }

    private val player by lazy {
        AndroidAudioPlayer(applicationContext)
    }

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var deviceHelper: DeviceHelper
    private lateinit var imageURI: Uri
    private lateinit var binding: ActivityMainBinding
    private lateinit var chatSessionInfo: ChatSessionInfo
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: UserLocation

    private var fileHelper = FileHelper()
    private var currentImagePath: String? = null
    private var audioFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.chatToolbar)
        initialize()
        configureSessionChatInfo()
        configureMainView()
        configureGoogleMaps()

        loadStoredMessages()
        configureListenerForSendMessages()
        configureListenerForAudioRecorder()

        addWatcherToShowHideSendButton(binding.chatInputMessage, binding.chatSendMessageIcon)
        configureGetterForUserLocation()
    }

    private fun configureGoogleMaps() {
        //binding.chatRecycler
    }

    private fun configureListenerForAudioRecorder() {
        val recordPermission = Manifest.permission.RECORD_AUDIO

        // Solicitar permissão, se não concedida
        if (ActivityCompat.checkSelfPermission(
                this,
                recordPermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(recordPermission), 200)
        }

        binding.audioRecorder.setOnClickListener {
            // Alterna entre iniciar e parar a gravação
            if (recorder.isRecording()) {
                stopAudioRecording()
            } else {
                startAudioRecording()
            }
        }
    }

    private fun startAudioRecording() {
        // Define o caminho do arquivo de áudio a ser salvo
        audioFile = File(filesDir, "audio_${System.currentTimeMillis()}.mp4")

        // Inicializa o gravador de áudio
        recorder.start(audioFile!!)

        // Atualizar UI (exemplo: alterar ícone para "parar")
        binding.audioRecorder.setImageResource(R.drawable.sharp_mic_off_24)
    }

    private fun stopAudioRecording() {
        // Para a gravação de áudio
        recorder.stop()

        // Atualizar UI (exemplo: alterar ícone para "microfone")
        binding.audioRecorder.setImageResource(R.drawable.baseline_mic_24)

        // Aqui você pode adicionar o áudio como uma mensagem
        audioFile?.let {
            val messageObject = Message(null, chatSessionInfo.getSenderUID(), it.absolutePath)
            messageObject.messageType = MessageType.AUDIO
            messageList.add(messageObject)
            messageAdapter.notifyDataSetChanged()
        }
    }

    private fun configureListenerForSendMessages() {
        // add the message to database
        binding.chatSendMessageIcon.setOnClickListener {
            val message = binding.chatInputMessage.text.toString()

            if (currentImagePath != null) {
                val messageObject = Message(null, chatSessionInfo.getSenderUID(), currentImagePath)
                messageObject.messageType = MessageType.IMAGE
                messageList.add(messageObject)
            }

            val messageObject = Message()
            messageObject.message = message
            messageObject.messageType = MessageType.TEXT
            messageObject.sentId = chatSessionInfo.getSenderUID()
            messageObject.setUserLocation(currentLocation)
            messageList.add(messageObject)
            fileHelper.writeMessages(messageList, applicationContext)


            // Integração com o UDINE
            val mapMessage = Message()
            mapMessage.messageType = MessageType.MAP
            mapMessage.sentId = "SYSTEM"
            mapMessage.setUserLocation(messageObject.getUserLocation())
            messageList.add(mapMessage)
            fileHelper.writeMessages(messageList, applicationContext)

            currentImagePath = null
            messageAdapter.notifyDataSetChanged()

            binding.chatInputMessage.setText("")
            binding.chatRecycler.scrollToPosition(messageList.size - 1)
        }

    }

    private fun loadStoredMessages() {
        val storedMessageList = fileHelper.readMessages(applicationContext)
        messageList.clear()
        messageList.addAll(storedMessageList)
    }

    private fun configureMainView() {
        binding.chatRecycler.layoutManager = LinearLayoutManager(this)
        binding.chatRecycler.adapter = messageAdapter
        binding.chatRecycler.scrollToPosition(messageList.size - 1)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.chatRecycler.layoutManager = layoutManager
    }

    private fun configureGetterForUserLocation() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION, false
                ) -> {
                    // Permissão concedida para localização precisa
                    getCurrentLocation()
                }

                permissions.getOrDefault(
                    Manifest.permission.ACCESS_COARSE_LOCATION, false
                ) -> {
                    // Permissão concedida para localização aproximada
                    getCurrentLocation()
                }

                else -> {
                    // Nenhuma permissão concedida
                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun getCurrentLocation() {
        val locationRequest = LocationRequest
            .Builder(TimeUnit.SECONDS.toMillis(30))
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5)).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                if (lastLocation != null) {
                    currentLocation.latitude = lastLocation.latitude
                    currentLocation.longitude = lastLocation.longitude
                }
            }
        }

        // Solicitar atualizações de localização
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper() // Para executar o callback na thread principal
        )
    }

    private fun initialize() {
        this.imageURI = fileHelper.createImageURI(this)
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        this.currentLocation = UserLocation()
        // fusedLocationClient.removeLocationUpdates(locationCallback)

        this.messageList = ArrayList()
        this.messageAdapter = MessageAdapter(this, messageList, supportFragmentManager)
    }

    private fun configureSessionChatInfo() {
        this.deviceHelper = DeviceHelper()
        this.chatSessionInfo = ChatSessionInfo()
        this.chatSessionInfo.setSenderName("DeviceName")
        this.chatSessionInfo.setSenderUID(deviceHelper.getDeviceId(applicationContext))
        this.chatSessionInfo.setReceiverName(getString(R.string.app_name))
        this.chatSessionInfo.setReceiverUID(getReceiverUID())
        this.chatSessionInfo.setSenderRoom(chatSessionInfo.getReceiverUID() + chatSessionInfo.getSenderUID())
        this.chatSessionInfo.setReceiverRoom(chatSessionInfo.getSenderUID() + chatSessionInfo.getReceiverUID())
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
                    "Não"
                ) { dialogInterface, i ->
                    dialogInterface.cancel()
                }

                alert.setPositiveButton(
                    "Sim"
                ) { dialogInterface, i ->
                    // Limpa todas as mensagens
                    messageList.clear()
                    messageAdapter.notifyDataSetChanged()

                    // Limpa o armazenamento de mensagens (caso esteja salvando)
                    fileHelper.writeMessages(messageList, applicationContext)
                }

                alert.create().show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}