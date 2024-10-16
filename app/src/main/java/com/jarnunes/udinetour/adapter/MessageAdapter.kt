package com.jarnunes.udinetour.adapter

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.jarnunes.udinetour.R
import com.jarnunes.udinetour.helper.DeviceHelper
import com.jarnunes.udinetour.holder.ReceiveViewHolder
import com.jarnunes.udinetour.holder.SentViewHolder
import com.jarnunes.udinetour.model.Message
import com.jarnunes.udinetour.model.MessageType
import com.jarnunes.udinetour.recorder.AndroidAudioPlayer

class MessageAdapter(private val context: Context, private val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<ViewHolder>() {

    private var deviceHelper = DeviceHelper()
    private val itemReceiveCode = 1
    private val itemSentCode = 2

    private val player by lazy {
        AndroidAudioPlayer(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 1) {
            // inflate receive
            val view: View = LayoutInflater.from(context).inflate(R.layout.receive, parent, false)
            return ReceiveViewHolder(view)
        } else {
            // inflate sent
            val view: View = LayoutInflater.from(context).inflate(R.layout.sent, parent, false)
            return SentViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]

        return if (deviceHelper.getDeviceId(context) == currentMessage.sentId)
            itemSentCode
        else
            itemReceiveCode
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        if (holder.javaClass == SentViewHolder::class.java) {
            val viewHolder = holder as SentViewHolder
            when (currentMessage.messageType) {
                MessageType.IMAGE -> {
                    holder.sentImage.visibility = View.VISIBLE
                    holder.sentAudioLayout.visibility = View.GONE
                    holder.sentMessage.visibility = View.GONE
                    holder.sentImage.setImageURI(Uri.parse(currentMessage.resourcePath))
                }

                MessageType.TEXT -> {
                    holder.sentMessage.visibility = View.VISIBLE
                    holder.sentImage.visibility = View.GONE
                    holder.sentAudioLayout.visibility = View.GONE
                    holder.sentMessage.text = currentMessage.message
                }

                MessageType.AUDIO -> {
                    holder.sentAudioLayout.visibility = View.VISIBLE
                    holder.sentAudio.visibility = View.VISIBLE
                    holder.sentAudioSeekBar.visibility = View.VISIBLE
                    holder.sentAudioDuration.visibility = View.VISIBLE
                    holder.sentMessage.visibility = View.GONE
                    holder.sentImage.visibility = View.GONE

                    // Configurar o layout de áudio
                    setupAudioPlayer(
                        holder.sentAudio,
                        holder.sentAudioSeekBar,
                        holder.sentAudioDuration,
                        currentMessage.resourcePath!!
                    )

                }

                null -> {
                    /*Do nothing*/
                }
            }

        } else {
            // do dtuff for receive view holder
            val viewHolder = holder as ReceiveViewHolder
            holder.receiveMessage.text = currentMessage.message
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