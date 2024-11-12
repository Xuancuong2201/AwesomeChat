package com.example.awesomechat.view.tablayoutfriend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.awesomechat.adapter.FriendAdapter
import com.example.awesomechat.databinding.FragmentFriendRequestBinding
import com.example.awesomechat.interact.Swipe
import com.example.awesomechat.viewmodel.FriendViewModel
import com.example.awesomechat.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendRequestFragment : Fragment() {
    private lateinit var binding: FragmentFriendRequestBinding
    private lateinit var adapter: FriendAdapter
    private lateinit var adapterRequest: FriendAdapter
    private val viewModel: FriendViewModel by viewModels<FriendViewModel>()
    private val searchViewModel: SearchViewModel by activityViewModels<SearchViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        adapter = FriendAdapter(viewModel)
        adapterRequest = FriendAdapter(viewModel)
        binding = FragmentFriendRequestBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.invitationList.observe(viewLifecycleOwner) {
            adapter.setItems(it ?: emptyList())
            binding.progressBar.visibility = View.GONE
            binding.rcvInvitationFriend.let { rcv ->
                rcv.layoutManager = LinearLayoutManager(requireContext())
                rcv.adapter = adapter
            }
        }

        viewModel.requestList.observe(viewLifecycleOwner) {
            adapterRequest.setItems(it ?: emptyList())
            binding.rcvAddFriend.let { rcv ->
                rcv.layoutManager = LinearLayoutManager(requireContext())
                rcv.adapter = adapterRequest
            }
        }
        searchViewModel.searchQuery.observe(viewLifecycleOwner) {
            adapter.filter.filter(it)
            adapterRequest.filter.filter(it)
        }
        val swipeCallback = Swipe(requireContext())
        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.rcvInvitationFriend)
    }
}

