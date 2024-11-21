package com.example.awesomechat.view


import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.awesomechat.R
import com.example.awesomechat.adapter.MessagesAdapter
import com.example.awesomechat.databinding.FragmentMessageBinding
import com.example.awesomechat.model.Messages
import com.example.awesomechat.viewmodel.ChatViewModel

import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentMessage : Fragment() {
    private lateinit var binding: FragmentMessageBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var controller: NavController
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        controller = (activity as MainActivity).findNavController(R.id.fragment)
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

        binding.rcvMessage.let { rcv ->
            rcv.layoutManager = LinearLayoutManager(requireContext())
            rcv.adapter = adapter
        }
        viewModel.messageList.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = View.GONE
            adapter.submitList(it.toList())
        }
        binding.btnCreateMessage.setOnClickListener {
            controller.navigate(R.id.action_homeFragment_to_fragmentCreateMess)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.searchMessage.setOnQueryTextListener(object :
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { adapter.filter.filter(it) }
                return true
            }
        })

    }

}