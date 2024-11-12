package com.example.awesomechat.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.awesomechat.databinding.ItemMessageRecipientSelectBinding
import com.example.awesomechat.model.User
import com.example.awesomechat.viewmodel.CreateMessViewModel

class RecipientMessageAdapter(val viewModel: CreateMessViewModel) :
    ListAdapter<User, RecyclerView.ViewHolder>(IUserDiffUtil()), Filterable {
    private var originalList: List<User> = emptyList()

    class ItemUser(
        private val itemBinding: ItemMessageRecipientSelectBinding,
        val viewModel: CreateMessViewModel,
        val adapter: RecipientMessageAdapter
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: User, position: Int) {
            itemBinding.user = item
            itemBinding.cbSelect.isChecked = (position == viewModel.position.value)
            itemBinding.frameRecipientSelect.setOnClickListener {
                val isChecked = itemBinding.cbSelect.isChecked
                itemBinding.cbSelect.isChecked = !isChecked
                viewModel.user.postValue(if (isChecked) null else item)
                viewModel.position.postValue(if (isChecked) -1 else position)
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


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemUser -> holder.bind(getItem(position), position)
            else -> throw IllegalArgumentException("Invalid ViewType")
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filteredList: MutableList<User> = mutableListOf()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(originalList)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (it in originalList) {
                        if (it.name.lowercase().contains(filterPattern)) {
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
                    val filteredUsers = values.filterIsInstance<User>()
                    submitList(filteredUsers)
                }
            }
        }
    }

    fun setItems(list: List<User>) {
        originalList = list
        submitList(list)
    }
}

class IUserDiffUtil : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return areItemsTheSame(oldItem, newItem)
    }
}

