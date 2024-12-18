package com.example.awesomechat.view


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.awesomechat.R
import com.example.awesomechat.adapter.MessagesAdapter
import com.example.awesomechat.databinding.FragmentMessageBinding
import com.example.awesomechat.model.Messages
import com.example.awesomechat.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentMessage : FragmentBase<FragmentMessageBinding>() {
    private lateinit var adapter: MessagesAdapter
    private val viewModel: ChatViewModel by viewModels()

    override fun getFragmentView(): Int {
        return R.layout.fragment_message
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MessagesAdapter(object : MessagesAdapter.ItemClickListener {
            override fun onItemClick(position: Int, messages: Messages) {
                val action =
                    FragmentHomeDirections.actionHomeFragmentToFragmentDetailsMessage(messages)
                controllerRoot.navigate(action)
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
            controllerRoot.navigate(R.id.action_homeFragment_to_fragmentCreateMess)
        }
        requestPermissionNotification()
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

    private fun requestPermissionNotification() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            val content =
                if (isGranted) getString(R.string.notify_success) else getString(R.string.notify_failed)
            Toast.makeText(requireContext(), content, Toast.LENGTH_SHORT).show()
        }
}