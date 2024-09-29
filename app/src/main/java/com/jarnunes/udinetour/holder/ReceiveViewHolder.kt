package com.jarnunes.udinetour.holder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jarnunes.udinetour.R

class ReceiveViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
    val receiveMessage = itemView.findViewById<TextView>(R.id.txt_receive_message)
}