package com.example.awesomechat.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.awesomechat.databinding.ItemMessageRecipientBinding
import com.example.awesomechat.databinding.ItemMessageUserBinding
import com.example.awesomechat.model.DetailMessage
import com.example.awesomechat.viewmodel.DetailsMessageViewModel

class DetailsMessageAdapter(val viewmodel: DetailsMessageViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val itemListRoot = arrayListOf<DetailMessage>()

    class ItemMessageRecipient(
        private val itemRecipient: ItemMessageRecipientBinding,
        private val viewmodel: DetailsMessageViewModel
    ) : RecyclerView.ViewHolder(itemRecipient.root) {
        private var multiImageAdapter = MultiImageAdapter()
        fun bind(item: DetailMessage) {
            itemRecipient.message = item
            itemRecipient.tvTime.visibility = if (item.show) View.VISIBLE else View.GONE
            itemRecipient.frameRecipient.setOnClickListener {
                itemRecipient.tvTime.visibility =
                    if (itemRecipient.tvTime.visibility == View.GONE) View.VISIBLE else View.GONE
            }
            if (item.type == "multi image") {
                val spanCount = if (item.multiImage!!.size == 2) 2 else 3
                itemRecipient.rcvMultiImage.apply {
                    layoutManager = GridLayoutManager(context, spanCount)
                    adapter = multiImageAdapter
                    multiImageAdapter.submitList(item.multiImage)
                }
            }
            itemRecipient.viewmodel = viewmodel
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
            if (item.type == "multi image") {
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
                ), viewmodel
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

    override fun getItemCount(): Int {
        return itemListRoot.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemMessageRecipient -> holder.bind(itemListRoot[position])
            is ItemMessageUser -> holder.bind(itemListRoot[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemListRoot[position].sentby) {
            "user" -> 2
            "recipient" -> 1
            else -> throw IllegalArgumentException("Invalid ViewType , $itemListRoot[position].sentby")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(updateList: List<DetailMessage>) {
        itemListRoot.clear()
        itemListRoot.addAll(updateList)
        notifyDataSetChanged()
    }
}