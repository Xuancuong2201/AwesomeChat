package com.example.awesomechat.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.awesomechat.databinding.ItemMessageRecipientSelectBinding
import com.example.awesomechat.model.User
import com.example.awesomechat.viewmodel.CreateMessViewModel

class RecipientMessageAdapter (val viewModel: CreateMessViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),Filterable {
    private val itemListRoot = arrayListOf<User>()
    private val itemListInteract = arrayListOf<User>()

    class ItemUser(
        private val itemBinding: ItemMessageRecipientSelectBinding,
        val viewModel: CreateMessViewModel,
        val adapter: RecipientMessageAdapter
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: User) {
            itemBinding.user = item
            itemBinding.cbSelect.isChecked = (adapterPosition == viewModel.position.value)
            itemBinding.frameRecipientSelect.setOnClickListener {
                val isChecked = itemBinding.cbSelect.isChecked
                itemBinding.cbSelect.isChecked = !isChecked
                viewModel.user.postValue(if (isChecked) null else item)
                viewModel.position.postValue(if (isChecked) -1 else adapterPosition)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemUser(
            ItemMessageRecipientSelectBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), viewModel, this
        )
    }

    override fun getItemCount(): Int {
        return itemListInteract.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemUser -> holder.bind(itemListInteract[position])
            else -> throw IllegalArgumentException("Invalid ViewType")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(updateList: List<User>) {
        itemListInteract.clear()
        itemListRoot.clear()
        itemListInteract.addAll(updateList)
        itemListRoot.addAll(updateList)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateListFilter(updateList: List<User>) {
        itemListInteract.clear()
        itemListInteract.addAll(updateList)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                itemListInteract.addAll(itemListRoot)
                val filteredList: MutableList<User> = mutableListOf()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(itemListRoot)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (it in itemListRoot) {
                        if (it.name.lowercase().contains(filterPattern)) {
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
}

