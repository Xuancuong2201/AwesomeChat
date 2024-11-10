package com.example.awesomechat.adapter

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.awesomechat.R
import com.example.awesomechat.databinding.ItemMessageBinding
import com.example.awesomechat.model.Messages


class MessagesAdapter(private val listener: ItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private val itemListRoot = arrayListOf<Messages>()
    private val itemListInteract = arrayListOf<Messages>()

    class ItemMessage(
        private val itemBinding: ItemMessageBinding,
        private val listener: ItemClickListener
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: Messages) {
            itemBinding.message = item

            if (item.type.equals("mess")) {
                itemBinding.tvCurrentMessage.text = item.currentMessage
            } else
                itemBinding.tvCurrentMessage.text =
                    itemBinding.root.context.getString(R.string.image)

            itemBinding.root.setOnClickListener {
                listener.onItemClick(adapterPosition, item)
            }
            when (item.quantity) {
                0 -> {
                    itemBinding.notification.visibility = View.GONE
                    itemBinding.borderImage.visibility = View.GONE
                    itemBinding.tvCurrentMessage.setTypeface(null, Typeface.NORMAL);
                }

                in 1..9 -> {
                    itemBinding.notification.visibility = View.VISIBLE
                    itemBinding.borderImage.visibility = View.VISIBLE
                    itemBinding.quantityMess.text = item.quantity.toString()
                    itemBinding.tvCurrentMessage.setTypeface(null, Typeface.BOLD);
                }

                else -> {
                    itemBinding.quantityMess.text = "+9"
                    itemBinding.notification.visibility = View.VISIBLE
                    itemBinding.borderImage.visibility = View.VISIBLE
                    itemBinding.tvCurrentMessage.setTypeface(null, Typeface.BOLD);
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

    override fun getItemCount(): Int {
        return itemListInteract.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemMessage -> holder.bind(itemListInteract[position])
        }
    }

    fun updateList(updateList: List<Messages>) {
        itemListInteract.clear()
        itemListRoot.clear()
        itemListInteract.addAll(updateList)
        itemListRoot.addAll(updateList)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateListFilter(updateList: List<Messages>) {
        itemListInteract.clear()
        itemListInteract.addAll(updateList)
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onItemClick(position: Int, messages: Messages)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                itemListInteract.addAll(itemListRoot)
                val filteredList: MutableList<Messages> = mutableListOf()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(itemListRoot)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (it in itemListRoot) {
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
                    val messages = values.filterIsInstance<Messages>()
                    updateListFilter(messages)
                }
            }
        }
    }
}