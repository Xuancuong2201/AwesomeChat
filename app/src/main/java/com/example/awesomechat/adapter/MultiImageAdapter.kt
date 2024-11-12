package com.example.awesomechat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.awesomechat.R

class MultiImageAdapter :
    ListAdapter<String, MultiImageAdapter.MultiImageViewHolder>(ImageDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiImageViewHolder {
        return MultiImageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MultiImageViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }

    class MultiImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var imageView = view.findViewById<ImageView>(R.id.image)!!
        fun bindData(uri: String) {
            imageView.setImageURI(uri.toUri())
            Glide.with(imageView)
                .load(uri)
                .into(imageView)
        }
    }
}