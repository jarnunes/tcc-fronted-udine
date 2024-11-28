package com.jarnunes.udinetour.holder

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.MapView
import com.jarnunes.udinetour.R

class ReceiveViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    val receiveMessage = itemView.findViewById<TextView>(R.id.txt_receive_message)
    val receiveAudioLayout = itemView.findViewById<LinearLayout>(R.id.receiveAudioMessageLayout)
    val receiveAudio = itemView.findViewById<ImageView>(R.id.receivePlayAudioButton)
    val receiveAudioDuration = itemView.findViewById<TextView>(R.id.receiveAudioDuration)
    val receiveAudioSeekBar = itemView.findViewById<SeekBar>(R.id.receiveAudioSeekBar)
    var receiveMapView = itemView.findViewById<MapView>(R.id.map_view)
    var receiveImageGallery = itemView.findViewById<RecyclerView>(R.id.receiveImageGallery)

}