package com.example.awesomechat.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.awesomechat.adapter.RecipientMessageAdapter
import com.example.awesomechat.databinding.FragmentCreateMessBinding
import com.example.awesomechat.model.Messages
import com.example.awesomechat.viewmodel.CreateMessViewModel
import com.example.awesomechat.viewmodel.FriendViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentCreateMess : Fragment() {
    private lateinit var binding: FragmentCreateMessBinding
    private lateinit var adapterRecipient: RecipientMessageAdapter
    private lateinit var controller: NavController
    private val viewModelFriend: FriendViewModel by viewModels()
    private val viewModelCreateMess: CreateMessViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        controller = findNavController()
        adapterRecipient = RecipientMessageAdapter(viewModelCreateMess)
        binding = FragmentCreateMessBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelFriend.friendList.observe(viewLifecycleOwner) { it ->
            adapterRecipient.setItems(it)
            binding.rcvCreateMessage.let { rcv ->
                rcv.layoutManager = LinearLayoutManager(requireContext())
                rcv.adapter = adapterRecipient
            }
            viewModelCreateMess.user.observe(viewLifecycleOwner) {
                if (it == null) {
                    binding.frameFriend.visibility = View.GONE
                } else {
                    binding.user = it
                    adapterRecipient.submitList(viewModelFriend.friendList.value!!)
                    binding.frameFriend.visibility = View.VISIBLE
                }
            }
        }

        binding.btnCreateMessage.setOnClickListener {
            val message = Messages(
                url = viewModelCreateMess.user.value!!.url,
                name = viewModelCreateMess.user.value!!.name,
                email = viewModelCreateMess.user.value!!.email
            )
            val action =
                FragmentCreateMessDirections.actionFragmentCreateMessToFragmentDetailsMessage(
                    message
                )
            hideFrameAndReset()
            controller.navigate(action)

        }

        binding.btnDelete.setOnClickListener {
            hideFrameAndReset()
        }

        binding.btnBack.setOnClickListener {
            hideFrameAndReset()
            controller.popBackStack()
        }

        binding.searchFriend.setOnQueryTextListener(object :
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null)
                    adapterRecipient.filter.filter(newText)
                return true
            }
        })
    }

    private fun hideFrameAndReset() {
        binding.frameFriend.visibility = View.GONE
        viewModelCreateMess.position.postValue(-1)
        viewModelCreateMess.user.postValue(null)
    }
}
