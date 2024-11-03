package com.example.awesomechat.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.awesomechat.MainActivity
import com.example.awesomechat.R
import com.example.awesomechat.adapter.MessagesAdapter
import com.example.awesomechat.databinding.FragmentMessageBinding
import com.example.awesomechat.model.Messages
import com.example.awesomechat.repository.firebase.MessageRepository
import com.example.awesomechat.viewmodel.ChatViewModel
import com.example.awesomechat.viewmodel.factory.ViewModelChatFactory


class FragmentMessage : Fragment() {
    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: MessagesAdapter
    private lateinit var controller: NavController
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_message, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        val factory = ViewModelChatFactory(MessageRepository())
        controller = (activity as MainActivity).findNavController(R.id.fragment)
        viewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MessagesAdapter(object : MessagesAdapter.ItemClickListener {
            override fun onItemClick(position: Int, messages: Messages) {
                val action =
                    FragmentHomeDirections.actionHomeFragmentToFragmentDetailsMessage(messages)
                controller.navigate(action)
                viewModel.changeStatus(messages.email.toString())
            }
        })
        viewModel.messageList.observe(viewLifecycleOwner) {
            adapter.updateList(it)
        }
        binding.rcvMessage.let { rcv ->
            rcv.layoutManager = LinearLayoutManager(requireContext())
            rcv.adapter = adapter
        }
        binding.btnCreateMessage.setOnClickListener {
            controller.navigate(R.id.action_homeFragment_to_fragmentCreateMess)
        }

    }
    override fun onStart() {
        super.onStart()
        binding.searchMessage.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { adapter.filter.filter(it) }
                return true
            }
        })
    }

}