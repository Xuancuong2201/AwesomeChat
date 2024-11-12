package com.example.awesomechat.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.awesomechat.R
import com.example.awesomechat.databinding.ItemMessageBinding
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.model.Messages

class MessagesAdapter(private val listener: ItemClickListener) :
    ListAdapter<Messages, RecyclerView.ViewHolder>(MessagesCallback()), Filterable {
    private var originalList: List<Messages> = emptyList()

    class ItemMessage(
        private val itemBinding: ItemMessageBinding,
        private val listener: ItemClickListener
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: Messages, position: Int) {
            itemBinding.message = item

            if (item.type.equals(InfoFieldQuery.TYPE_MESS)) {
                itemBinding.tvCurrentMessage.text = item.currentMessage
            } else
                itemBinding.tvCurrentMessage.text =
                    itemBinding.root.context.getString(R.string.image)

            itemBinding.root.setOnClickListener {
                listener.onItemClick(position, item)
            }
            when (item.quantity) {
                0 -> {
                    itemBinding.notification.visibility = View.GONE
                    itemBinding.borderImage.visibility = View.GONE
                    itemBinding.tvCurrentMessage.setTypeface(null, Typeface.NORMAL)
                }

                in 1..9 -> {
                    itemBinding.notification.visibility = View.VISIBLE
                    itemBinding.borderImage.visibility = View.VISIBLE
                    itemBinding.quantityMess.text = item.quantity.toString()
                    itemBinding.tvCurrentMessage.setTypeface(null, Typeface.BOLD)
                }

                else -> {
                    itemBinding.quantityMess.text = "+9"
                    itemBinding.notification.visibility = View.VISIBLE
                    itemBinding.borderImage.visibility = View.VISIBLE
                    itemBinding.tvCurrentMessage.setTypeface(null, Typeface.BOLD)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemMessage(
            ItemMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), listener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemMessage -> holder.bind(getItem(position), position)
        }
    }

    interface ItemClickListener {
        fun onItemClick(position: Int, messages: Messages)
    }

    fun setItems(list: List<Messages>) {
        originalList = list
        submitList(list)
    }

    class MessagesCallback : DiffUtil.ItemCallback<Messages>() {
        override fun areItemsTheSame(oldItem: Messages, newItem: Messages): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Messages, newItem: Messages): Boolean {
            return oldItem == newItem
        }

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filteredList: MutableList<Messages> = mutableListOf()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(originalList)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (it in originalList) {
                        if (it.name!!.lowercase().contains(filterPattern)) {
                            filteredList.add(it)
                        }
                    }
                }
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val values = results?.values
                if (values is List<*>) {
                    val filteredUsers = values.filterIsInstance<Messages>()
                    submitList(filteredUsers)
                }
            }
        }
    }
}


