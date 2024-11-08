package com.example.awesomechat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.awesomechat.R
import com.example.awesomechat.viewmodel.DetailsMessageViewModel

class ImageFromGalleryAdapter(private val imageClickInterface: ImageClickInterface) :
    ListAdapter<String, ImageFromGalleryAdapter.ImageViewHolder>(ImageDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_image_from_gallery, parent, false),imageClickInterface)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    class ImageViewHolder(view: View,private val imageClickInterface: ImageClickInterface) :
        RecyclerView.ViewHolder(view) {
        private var imageView = view.findViewById<ImageView>(R.id.image)!!
        fun bindData(uri: String) {
            imageView.setImageURI(uri.toUri())
            imageView.setOnClickListener {
                imageClickInterface.selectImage(bindingAdapterPosition,uri)
            }
        }
    }
    class ImageDiffUtil : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return newItem == oldItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return areItemsTheSame(oldItem, newItem)
        }
    }

    interface ImageClickInterface{
        fun selectImage(position: Int,uri: String)
    }
}
