package com.example.awesomechat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.awesomechat.R
import com.example.awesomechat.databinding.ItemFriendBinding
import com.example.awesomechat.databinding.ItemHeaderBinding
import com.example.awesomechat.interact.InteractData
import com.example.awesomechat.model.User
import com.example.awesomechat.viewmodel.FriendViewModel

class FriendAdapter(val viewmodel: FriendViewModel) :
    ListAdapter<Any, RecyclerView.ViewHolder>(FriendDiffCallback()),Filterable {
    class ItemUser(
        private val itemBinding: ItemFriendBinding,
        private val viewmodel: FriendViewModel,
        private val adapter: FriendAdapter
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: User) {
            itemBinding.user = item
            when (item.state) {
                "friend" ->itemBinding.btSelect.visibility = View.GONE
                "request" -> itemBinding.btSelect.apply {
                    text = "Hủy"
                    setTextColor(
                        ContextCompat.getColor(
                            itemBinding.root.context,
                            R.color.primary_color
                        )
                    )
                    setBackgroundResource(R.drawable.custom_button_sented)
                    setOnClickListener {
                        viewmodel.cancelRequestFriend(item.email)
                    }
                }

                "user" -> {
                    itemBinding.btSelect.apply {
                        text = "Kết bạn"
                        setOnClickListener {
                            viewmodel.sendRequestFriend(item.email)
                        }
                    }
                }

                "invitation" -> {
                    itemBinding.btDelete.setOnClickListener {
                        viewmodel.refuseInvitationFriend(item.email)
                    }
                    itemBinding.btSelect.setOnClickListener {
                        viewmodel.acceptInvitationFriend(item.email)
                    }
                }
            }
        }
    }

    class ItemHeader(private val itemHeaderBinding: ItemHeaderBinding) :
        RecyclerView.ViewHolder(itemHeaderBinding.root) {
        fun bind(header: Char) {
            itemHeaderBinding.header = header.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            InteractData.TYPE_ITEM -> ItemUser(
                ItemFriendBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), viewmodel, this
            )

            InteractData.TYPE_HEADER -> ItemHeader(
                ItemHeaderBinding.inflate(
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
            is ItemHeader -> holder.bind(getItem(position) as Char)
            is ItemUser -> holder.bind(getItem(position) as User)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is User -> InteractData.TYPE_ITEM
            is Char -> InteractData.TYPE_HEADER
            else -> throw IllegalArgumentException("Invalid ViewType")
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filteredList: MutableList<Any> = mutableListOf()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(currentList)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (it in currentList) {
                        if (it is User && it.name.lowercase().contains(filterPattern)) {
                            filteredList.add(it)
                        }
                    }
                }
                results.values = filteredList
                return results
            }
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val filteredUsers = (results?.values as? List<User>) ?: emptyList()
                updateListFilter(filteredUsers)
            }
        }
    }
    fun updateListFilter(filteredUsers: List<User>) {
        submitList(filteredUsers)
    }
    class FriendDiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is User && newItem is User -> oldItem.email == newItem.email
                oldItem is Char && newItem is Char -> oldItem == newItem
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return areItemsTheSame(oldItem, newItem)
        }
    }
}