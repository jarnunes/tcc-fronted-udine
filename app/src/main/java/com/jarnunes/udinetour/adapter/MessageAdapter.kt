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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.GONE
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.jarnunes.udinetour.MainActivity
import com.jarnunes.udinetour.R
import com.jarnunes.udinetour.commons.TextUtils
import com.jarnunes.udinetour.helper.DeviceHelper
import com.jarnunes.udinetour.helper.FileHelper
import com.jarnunes.udinetour.holder.ReceiveViewHolder
import com.jarnunes.udinetour.holder.SentViewHolder
import com.jarnunes.udinetour.maps.MapService
import com.jarnunes.udinetour.message.ImageMessage
import com.jarnunes.udinetour.message.MapMessage
import com.jarnunes.udinetour.message.Message
import com.jarnunes.udinetour.message.MessageType
import com.jarnunes.udinetour.recorder.AndroidAudioPlayer

class MessageAdapter(
    private val mainActivity: MainActivity,
    private val messageList: ArrayList<Message>,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<ViewHolder>() {


    private var mapService: MapService? = null
    private var deviceHelper = DeviceHelper()
    private val itemReceiveCode = 1
    private val itemSentCode = 2
    private var currentMediaPlayer: MediaPlayer? = null
    private var currentPlayButton: ImageView? = null
    private var isMapInitialized = false

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
        viewHolder.sentMessage.visibility = View.GONE
        viewHolder.sentImage.visibility = View.GONE
        viewHolder.sentAudioLayout.visibility = View.GONE

        when (currentMessage.messageType) {
            MessageType.IMAGE -> {
                viewHolder.sentImage.visibility = View.VISIBLE
                viewHolder.sentImage.setImageURI(Uri.parse(currentMessage.resourcePath))
            }

            MessageType.TEXT -> {
                viewHolder.sentMessage.visibility = View.VISIBLE
                viewHolder.sentMessage.text = currentMessage.message
            }

            MessageType.AUDIO -> {
                viewHolder.sentAudioLayout.visibility = View.VISIBLE
                viewHolder.sentAudio.visibility = View.VISIBLE
                viewHolder.sentAudioSeekBar.visibility = View.VISIBLE
                viewHolder.sentAudioDuration.visibility = View.VISIBLE

                // Configurar o layout de áudio
                setupAudioPlayer(
                    viewHolder.sentAudio,
                    viewHolder.sentAudioSeekBar,
                    viewHolder.sentAudioDuration,
                    currentMessage.resourcePath!!
                )
            }

            MessageType.SYSTEM_WAIT, MessageType.SYSTEM_WAIT_START, MessageType.MAP,
            MessageType.LOCATION -> {
            }
        }
    }

    private fun initMapService(containerViewId: Int) {
        if (mapService == null) {
            mapService = MapService(containerViewId, mainActivity, fragmentManager)
        }
    }

    private fun configureReceiveViewHolder(holder: ViewHolder, currentMessage: Message) {
        val viewHolder = holder as ReceiveViewHolder
        viewHolder.receiveMessage.visibility = View.GONE
        viewHolder.receiveAudio.visibility = View.GONE
        viewHolder.receiveAudioLayout.visibility = View.GONE
        viewHolder.receiveAudioDuration.visibility = View.GONE
        viewHolder.receiveAudioSeekBar.visibility = View.GONE
        viewHolder.receiveMap.visibility = GONE

        when (currentMessage.messageType) {
            MessageType.TEXT, MessageType.SYSTEM_WAIT,  MessageType.SYSTEM_WAIT_START -> {
                viewHolder.receiveMessage.text = currentMessage.message
                viewHolder.receiveMessage.visibility = View.VISIBLE
                TextUtils.applyBoldToAsterisks(viewHolder.receiveMessage, currentMessage.message!!)
            }

            MessageType.AUDIO -> {
                viewHolder.receiveAudio.visibility = View.VISIBLE
                viewHolder.receiveAudioLayout.visibility = View.VISIBLE
                viewHolder.receiveAudioDuration.visibility = View.VISIBLE
                viewHolder.receiveAudioSeekBar.visibility = View.VISIBLE

                setupAudioPlayer(
                    viewHolder.receiveAudio,
                    viewHolder.receiveAudioSeekBar,
                    viewHolder.receiveAudioDuration,
                    currentMessage.resourcePath!!
                )
            }

            MessageType.MAP -> {
                if (!isMapInitialized) {
                    initMapService(viewHolder.receiveMap.id)
                    isMapInitialized = true
                }

                viewHolder.receiveMap.visibility = View.VISIBLE
                mapService?.createMap(currentMessage as MapMessage)
            }

            MessageType.IMAGE -> {
                val imageMessage = currentMessage as ImageMessage
                if (imageMessage.pathNames.isNotEmpty()) {
                    val files = FileHelper().readFilesAsByteArray(imageMessage.pathNames)
                    val adapter = ImageGalleryAdapter(files)
                    viewHolder.receiveImageGallery.adapter = adapter
                    viewHolder.receiveImageGallery.layoutManager =
                        LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
                    viewHolder.receiveImageGallery.visibility = View.VISIBLE
                }
            }
            MessageType.LOCATION -> {}
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
                println("MediaPlayer error: what = $what, extra = $extra")
                return@setOnErrorListener true
            }

            mediaPlayer.setOnPreparedListener {
                // Mostrar duração do áudio quando estiver pronto
                val duration = mediaPlayer.duration / 1000
                durationText.text = "${duration / 60}:${duration % 60}"

                seekBar.max = mediaPlayer.duration

                // Manipulando o botão de play/pause
                playButton.setOnClickListener {
                    if (currentMediaPlayer?.audioSessionId != mediaPlayer.audioSessionId
                        && currentMediaPlayer?.isPlaying == true) {
                        currentMediaPlayer?.pause()
                        currentPlayButton?.setImageResource(R.drawable.round_play_arrow_24)
                    }

                    currentMediaPlayer = mediaPlayer
                    currentPlayButton = playButton

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
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            mediaPlayer.seekTo(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        mediaPlayer.setOnCompletionListener {
            playButton.setImageResource(R.drawable.round_play_arrow_24)
            currentMediaPlayer = null
        }
    }


}