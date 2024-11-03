package com.example.awesomechat.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.awesomechat.R
import com.example.awesomechat.databinding.ItemFriendBinding
import com.example.awesomechat.databinding.ItemHeaderBinding
import com.example.awesomechat.interact.InteractData
import com.example.awesomechat.model.User
import com.example.awesomechat.viewmodel.FriendViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendAdapter(val viewmodel: FriendViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private val itemListInteract = arrayListOf<Any>()
    private val itemListRoot = arrayListOf<Any>()

    class ItemUser(
        private val itemBinding: ItemFriendBinding,
        private val viewmodel: FriendViewModel,
        private val adapter: FriendAdapter
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: User) {
            itemBinding.user = item
            when (item.state) {
                "friend" -> itemBinding.btSelect.visibility = View.GONE
                "request" -> itemBinding.btSelect.let {
                    it.text = "Hủy"
                    it.setTextColor(
                        ContextCompat.getColor(
                            itemBinding.root.context,
                            R.color.primary_color
                        )
                    )
                    it.setBackgroundResource(R.drawable.custom_button_sented)
                    it.setOnClickListener {
                        adapter.removeUser(bindingAdapterPosition)
                        CoroutineScope(Dispatchers.IO).launch { viewmodel.cancelRequestFriend(item.email) }
                    }
                }

                "none" -> {
                    itemBinding.btSelect.text = "Kêt bạn"
                    itemBinding.btSelect.let {
                        it.setOnClickListener {
                            CoroutineScope(Dispatchers.IO).launch { viewmodel.sendRequestFriend(item.email) }
                            itemBinding.btSelect.let { button ->
                                button.text = "Hủy"
                                button.setTextColor(
                                    ContextCompat.getColor(
                                        itemBinding.root.context,
                                        R.color.primary_color
                                    )
                                )
                                button.setBackgroundResource(R.drawable.custom_button_sented)
                            }
                        }
                    }
                }

                "invitation" -> {
                    itemBinding.btDelete.setOnClickListener {
                        adapter.removeUser(bindingAdapterPosition)
                        CoroutineScope(Dispatchers.IO).launch {
                            viewmodel.refuseInvitationFriend(item.email)
                        }
                    }
                    itemBinding.btSelect.setOnClickListener {
                        adapter.removeUser(bindingAdapterPosition)
                        CoroutineScope(Dispatchers.IO).launch {
                            viewmodel.acceptInvitationFriend(item.email)
                        }
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
            is ItemHeader -> holder.bind(itemListInteract[position] as Char)
            is ItemUser -> holder.bind(itemListInteract[position] as User)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemListInteract[position]) {
            is User -> InteractData.TYPE_ITEM
            is Char -> InteractData.TYPE_HEADER
            else -> throw IllegalArgumentException("Invalid ViewType")
        }
    }

    override fun getItemCount(): Int {
        return itemListInteract.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                itemListInteract.addAll(itemListRoot)
                val filteredList: MutableList<Any> = mutableListOf()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(itemListRoot)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (it in itemListRoot) {
                        if (it is User && it.name.lowercase().contains(filterPattern)) {
                            filteredList.add(it)
                        }
                    }
                }
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                updateListFilter(
                    (results?.values as? List<User>)?.toMutableList() ?: mutableListOf()
                )
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(updateList: List<Any>) {
        itemListInteract.clear()
        itemListRoot.clear()
        itemListInteract.addAll(updateList)
        itemListRoot.addAll(updateList)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateListFilter(updateList: List<Any>) {
        itemListInteract.clear()
        itemListInteract.addAll(updateList)
        notifyDataSetChanged()
    }

    fun removeUser(position: Int) {
        itemListInteract.removeAt(position)
        notifyItemRemoved(position)
    }

}