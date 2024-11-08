package com.jarnunes.udinetour.adapter

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.GONE
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.jarnunes.udinetour.MainActivity
import com.jarnunes.udinetour.R
import com.jarnunes.udinetour.helper.DeviceHelper
import com.jarnunes.udinetour.holder.ReceiveViewHolder
import com.jarnunes.udinetour.holder.SentViewHolder
import com.jarnunes.udinetour.maps.PlaceType
import com.jarnunes.udinetour.maps.PlacesApiServiceImpl
import com.jarnunes.udinetour.message.Message
import com.jarnunes.udinetour.message.MessageType
import com.jarnunes.udinetour.message.UserLocation
import com.jarnunes.udinetour.recorder.AndroidAudioPlayer

class MessageAdapter(
    private val mainActivity: MainActivity,
    private val messageList: ArrayList<Message>,
    private val fragmentManager: FragmentManager
) :
    RecyclerView.Adapter<ViewHolder>() {

    private var deviceHelper = DeviceHelper()
    private val itemReceiveCode = 1
    private val itemSentCode = 2

    private val player by lazy {
        AndroidAudioPlayer(mainActivity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 1) {
            // inflate receive
            val view: View =
                LayoutInflater.from(mainActivity).inflate(R.layout.receive, parent, false)
            return ReceiveViewHolder(view, fragmentManager)
        } else {
            // inflate sent
            val view: View = LayoutInflater.from(mainActivity).inflate(R.layout.sent, parent, false)
            return SentViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]

        return if (deviceHelper.getUserDeviceId(mainActivity) == currentMessage.sentId)
            itemSentCode
        else
            itemReceiveCode
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        if (holder.javaClass == SentViewHolder::class.java) {
            configureSentViewHolder(holder, currentMessage)
        } else {
            configureReceiveViewHolder(holder, currentMessage)
        }
    }

    private fun configureSentViewHolder(holder: ViewHolder, currentMessage: Message) {
        val viewHolder = holder as SentViewHolder
        when (currentMessage.messageType) {
            MessageType.IMAGE -> {
                viewHolder.sentImage.visibility = View.VISIBLE
                viewHolder.sentAudioLayout.visibility = View.GONE
                viewHolder.sentMessage.visibility = View.GONE
                viewHolder.sentImage.setImageURI(Uri.parse(currentMessage.resourcePath))
            }

            MessageType.TEXT -> {
                viewHolder.sentMessage.visibility = View.VISIBLE
                viewHolder.sentImage.visibility = View.GONE
                viewHolder.sentAudioLayout.visibility = View.GONE
                viewHolder.sentMessage.text = currentMessage.message
            }

            MessageType.AUDIO -> {
                viewHolder.sentAudioLayout.visibility = View.VISIBLE
                viewHolder.sentAudio.visibility = View.VISIBLE
                viewHolder.sentAudioSeekBar.visibility = View.VISIBLE
                viewHolder.sentAudioDuration.visibility = View.VISIBLE
                viewHolder.sentMessage.visibility = View.GONE
                viewHolder.sentImage.visibility = View.GONE

                // Configurar o layout de áudio
                setupAudioPlayer(
                    viewHolder.sentAudio,
                    viewHolder.sentAudioSeekBar,
                    viewHolder.sentAudioDuration,
                    currentMessage.resourcePath!!
                )
            }

            MessageType.MAP -> {}
            MessageType.LOCATION -> {}
        }
    }

    private fun configureReceiveViewHolder(holder: ViewHolder, currentMessage: Message) {
        val viewHolder = holder as ReceiveViewHolder

        when (currentMessage.messageType) {
            MessageType.TEXT -> {
                viewHolder.receiveMessage.text = currentMessage.message
                viewHolder.receiveMessage.visibility = View.VISIBLE
            }

            MessageType.AUDIO -> {
                viewHolder.receiveMessage.visibility = View.GONE
                viewHolder.receiveMap.visibility = GONE

                viewHolder.receiveAudio.visibility = View.VISIBLE
                viewHolder.receiveAudioLayout.visibility = View.VISIBLE
                viewHolder.receiveAudioDuration.visibility = View.VISIBLE
                viewHolder.receiveAudioSeekBar.visibility = View.VISIBLE
            }

            MessageType.MAP -> {
                viewHolder.receiveMessage.visibility = View.GONE
                viewHolder.receiveAudio.visibility = View.GONE
                viewHolder.receiveAudioLayout.visibility = View.GONE
                viewHolder.receiveAudioDuration.visibility = View.GONE
                viewHolder.receiveAudioSeekBar.visibility = View.GONE
                viewHolder.receiveMap.visibility = View.VISIBLE

                val userLocation = currentMessage.getUserLocation()
                val mapFragment = SupportMapFragment.newInstance()
                fragmentManager.beginTransaction()
                    .replace(viewHolder.receiveMap.id, mapFragment)
                    .commit()

                mapFragment.getMapAsync { googleMap ->
                    removeDefaultConfiguration(googleMap)
                    addCurrentLocationPointMarker(googleMap, userLocation)
                    addTouristsPointMarker(googleMap, userLocation)
                }
            }

            MessageType.IMAGE -> {}
            MessageType.LOCATION -> {}
        }
    }

    private fun removeDefaultConfiguration(googleMap: GoogleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mainActivity, R.raw.map_style))
    }

    private fun addCurrentLocationPointMarker(googleMap: GoogleMap, userLocation: UserLocation) {
        val location = LatLng(userLocation.latitude!!, userLocation.longitude!!)
        val title = mainActivity.getString(R.string.maps_current_location)
        googleMap.addMarker(MarkerOptions().position(location).title(title))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun addTouristsPointMarker(googleMap: GoogleMap, userLocation: UserLocation) {
        PlacesApiServiceImpl(mainActivity).getNearbyPlaces(userLocation) { placesResult ->
            if (placesResult.isNotEmpty()) {
                placesResult.forEach { place ->
                    val placeLoc = place.geometry.location
                    val customIcon = PlaceType.bitmapDescriptorFactory(place.types)
                    val loc = LatLng(placeLoc.lat, placeLoc.lng)
                    val marker = googleMap.addMarker(
                        MarkerOptions().position(loc).title(place.name).icon(customIcon)
                    )

                    marker?.showInfoWindow()
                }
            } else {
                //implementar registro de logs
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setupAudioPlayer(
        playButton: ImageView,
        seekBar: SeekBar,
        durationText: TextView,
        audioPath: String
    ) {
        val mediaPlayer = MediaPlayer()

        try {
            mediaPlayer.setDataSource(audioPath)
            mediaPlayer.prepareAsync() // Preparação assíncrona

            // Listener de erro para capturar falhas
            mediaPlayer.setOnErrorListener { _, what, extra ->
                // Log e tratamento de erro
                println("MediaPlayer error: what = $what, extra = $extra")
                return@setOnErrorListener true
            }

            mediaPlayer.setOnPreparedListener {
                // Mostrar duração do áudio quando estiver pronto
                val duration = mediaPlayer.duration / 1000
                durationText.text = "${duration / 60}:${duration % 60}"

                seekBar.max = mediaPlayer.duration

                playButton.setOnClickListener {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                        playButton.setImageResource(R.drawable.round_play_arrow_24)
                    } else {
                        mediaPlayer.start()
                        playButton.setImageResource(R.drawable.round_stop_24)
                    }
                }

                val handler = Handler(Looper.getMainLooper())
                handler.post(object : Runnable {
                    override fun run() {
                        if (mediaPlayer.isPlaying) {
                            seekBar.progress = mediaPlayer.currentPosition
                        }
                        handler.postDelayed(this, 100)
                    }
                })

                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            mediaPlayer.seekTo(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            }

            mediaPlayer.setOnCompletionListener {
                playButton.setImageResource(R.drawable.round_play_arrow_24)
            }

        } catch (e: Exception) {
            // Tratamento de erro ao configurar o MediaPlayer
            e.printStackTrace()
        }

        mediaPlayer.setOnCompletionListener {
            playButton.setImageResource(R.drawable.round_play_arrow_24)
        }
    }


}