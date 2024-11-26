package com.jarnunes.udinetour.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.jarnunes.udinetour.R

class ImageGalleryAdapter(private val images: List<ByteArray>) :
    RecyclerView.Adapter<ImageGalleryAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_gallery, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val byteArray = images[position]
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        holder.imageView.setImageBitmap(bitmap)
    }

    override fun getItemCount() = images.size
}