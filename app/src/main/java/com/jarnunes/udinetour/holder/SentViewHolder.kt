package com.jarnunes.udinetour.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jarnunes.udinetour.R

class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val sentMessage = itemView.findViewById<TextView>(R.id.txt_sent_message)
    val sentImage = itemView.findViewById<ImageView>(R.id.img_sent_message)
}