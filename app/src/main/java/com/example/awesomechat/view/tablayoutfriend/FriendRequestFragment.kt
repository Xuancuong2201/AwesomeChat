package com.example.awesomechat.view.tablayoutfriend

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.awesomechat.R
import com.example.awesomechat.adapter.FriendAdapter
import com.example.awesomechat.databinding.FragmentFriendRequestBinding
import com.example.awesomechat.interact.InteractData
import com.example.awesomechat.interact.Swipe
import com.example.awesomechat.viewmodel.FriendViewModel
import com.example.awesomechat.viewmodel.SearchViewModel


class FriendRequestFragment : Fragment() {
    private var _binding: FragmentFriendRequestBinding? = null
    private val binding get() = _binding!!
    private  val viewModel: FriendViewModel by activityViewModels()
    private val searchViewModel: SearchViewModel by activityViewModels()
    private lateinit var adapter: FriendAdapter
    private lateinit var adapterRequest :FriendAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        adapter = FriendAdapter(viewModel)
        adapterRequest = FriendAdapter(viewModel)
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_friend_request, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.invitationList.observe(viewLifecycleOwner) {
            adapter.submitList(it?.let {it} ?: emptyList())
            binding.progressBar.visibility = View.GONE
            binding.rcvInvitationFriend.let { rcv ->
                rcv.layoutManager = LinearLayoutManager(requireContext())
                rcv.adapter = adapter
            }
        }

        viewModel.requestList.observe(viewLifecycleOwner) {
            adapterRequest.submitList(it ?: emptyList())
                adapterRequest.submitList(it)
                binding.rcvAddFriend.let { rcv ->
                    rcv.layoutManager = LinearLayoutManager(requireContext())
                    rcv.adapter = adapterRequest
                }
            }
        searchViewModel.searchQuery.observe(viewLifecycleOwner) {
            if(it.isEmpty()){
                adapter.submitList(viewModel.invitationList.value)
                adapterRequest.submitList(viewModel.requestList.value)
            }
            else
            adapter.filter.filter(it)
        }
        val swipeCallback = Swipe(requireContext())
        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.rcvInvitationFriend)
    }
}

