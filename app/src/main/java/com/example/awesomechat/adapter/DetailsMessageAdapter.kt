package com.example.awesomechat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.awesomechat.databinding.ItemMessageRecipientBinding
import com.example.awesomechat.databinding.ItemMessageUserBinding
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.model.DetailMessage
import com.example.awesomechat.viewmodel.DetailsMessageViewModel

class DetailsMessageAdapter(private  val url:String) :
    ListAdapter<DetailMessage, RecyclerView.ViewHolder>(DetailsMessageCallBack()) {
    class ItemMessageRecipient(
        private val itemRecipient: ItemMessageRecipientBinding,
        private val url: String
    ) : RecyclerView.ViewHolder(itemRecipient.root) {
        private var multiImageAdapter = MultiImageAdapter()
        fun bind(item: DetailMessage) {
            itemRecipient.message = item
            itemRecipient.tvTime.visibility = if (item.show) View.VISIBLE else View.GONE
            itemRecipient.frameRecipient.setOnClickListener {
                itemRecipient.tvTime.visibility =
                    if (itemRecipient.tvTime.visibility == View.GONE) View.VISIBLE else View.GONE
            }
            if (item.type == InfoFieldQuery.MULTI_IMAGE) {
                val spanCount = if (item.multiImage!!.size == 2) 2 else 3
                itemRecipient.rcvMultiImage.apply {
                    layoutManager = GridLayoutManager(context, spanCount)
                    adapter = multiImageAdapter
                    multiImageAdapter.submitList(item.multiImage)
                }
            }
            itemRecipient.url = url
        }
    }

    class ItemMessageUser(private val itemSentMessage: ItemMessageUserBinding) :
        RecyclerView.ViewHolder(itemSentMessage.root) {

        private var multiImageAdapter = MultiImageAdapter()
        fun bind(item: DetailMessage) {
            itemSentMessage.message = item
            itemSentMessage.tvTime.visibility = if (item.show) View.VISIBLE else View.GONE
            itemSentMessage.frameMessage.setOnClickListener {
                itemSentMessage.tvTime.visibility =
                    if (itemSentMessage.tvTime.visibility == View.GONE) View.VISIBLE else View.GONE
            }
            if (item.type == InfoFieldQuery.MULTI_IMAGE) {
                val spanCount = if (item.multiImage!!.size == 2) 2 else 3
                itemSentMessage.rcvMultiImage.apply {
                    layoutManager = GridLayoutManager(context, spanCount)
                    adapter = multiImageAdapter
                    multiImageAdapter.submitList(item.multiImage)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> ItemMessageRecipient(
                ItemMessageRecipientBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), url = url
            )

            2 -> ItemMessageUser(
                ItemMessageUserBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Invalid ViewType")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemMessageRecipient -> holder.bind(getItem(position))
            is ItemMessageUser -> holder.bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).sentby) {
            InfoFieldQuery.KEY_USER -> 2
            InfoFieldQuery.KEY_RECIPIENT -> 1
            else -> throw IllegalArgumentException("Invalid ViewType")
        }
    }

    class DetailsMessageCallBack : DiffUtil.ItemCallback<DetailMessage>() {
        override fun areItemsTheSame(oldItem: DetailMessage, newItem: DetailMessage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DetailMessage, newItem: DetailMessage): Boolean {
            return oldItem == newItem
        }
    }
}
