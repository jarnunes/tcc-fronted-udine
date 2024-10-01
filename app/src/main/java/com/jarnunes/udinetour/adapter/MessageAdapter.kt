package com.jarnunes.udinetour.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.jarnunes.udinetour.R
import com.jarnunes.udinetour.helper.DeviceHelper
import com.jarnunes.udinetour.holder.ReceiveViewHolder
import com.jarnunes.udinetour.holder.SentViewHolder
import com.jarnunes.udinetour.model.Message

class MessageAdapter(private val context: Context, private val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<ViewHolder>() {

    private var deviceHelper = DeviceHelper()
    private val itemReceiveCode = 1
    private val itemSentCode = 2

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
            if (currentMessage.imagePath != null) {
                holder.sentImage.visibility = View.VISIBLE
                holder.sentMessage.visibility = View.GONE
                holder.sentImage.setImageURI(Uri.parse(currentMessage.imagePath))
            } else {
                holder.sentImage.visibility = View.GONE
                holder.sentMessage.visibility = View.VISIBLE
                holder.sentMessage.text = currentMessage.message
            }
        } else {
            // do dtuff for receive view holder
            val viewHolder = holder as ReceiveViewHolder
            holder.receiveMessage.text = currentMessage.message
        }
    }


}