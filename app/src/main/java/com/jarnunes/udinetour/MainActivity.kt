package com.jarnunes.udinetour

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.jarnunes.udinetour.model.UserLocation
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var deviceHelper: DeviceHelper
    private lateinit var imageURI: Uri
    private lateinit var binding: ActivityMainBinding
    private lateinit var chatSessionInfo: ChatSessionInfo
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var fileHelper = FileHelper()
    private var currentImagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.chatToolbar)

        initialize()
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
            val userLocation = getCurrentLocationOnce()
            messageObject.setUserLocation(userLocation)

            messageList.add(messageObject)

            val systemMessage = createSystemMessage()
            systemMessage.message += " | latitude: " + userLocation.latitude + " | longitude: " + userLocation.longitude
            messageList.add(systemMessage)

            binding.chatInputMessage.setText("")
            fileHelper.writeMessages(messageList, applicationContext)
            currentImagePath = null
            messageAdapter.notifyDataSetChanged()
        }

        setChatImageEventListener()
        addWatcherToShowHideSendButton(binding.chatInputMessage, binding.chatSendMessageIcon)

        // configure user location.
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
        val locationRequest = LocationRequest.Builder(TimeUnit.SECONDS.toMillis(30))
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5)).build()

        // Callback que será acionado quando houver uma nova localização
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Log.i("INFO", "Este é um log")
                    Log.i("INFO", "latitude: $latitude")
                    Log.i("INFO", "longitude: $longitude")
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

    private fun getCurrentLocationOnce(): UserLocation {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return UserLocation(null, null)
        }

        var userLocation = UserLocation(null, null)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            userLocation = UserLocation(location.latitude.toString(), location.longitude.toString())
        }.addOnFailureListener { exception ->
            Log.e("ERROR", "Erro ao obter localização: ${exception.message}")
        }

        return userLocation
    }

    private fun initialize() {
        this.imageURI = fileHelper.createImageURI(this)
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // fusedLocationClient.removeLocationUpdates(locationCallback)
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