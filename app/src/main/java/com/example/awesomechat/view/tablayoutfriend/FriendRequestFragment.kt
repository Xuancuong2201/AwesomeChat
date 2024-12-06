package com.example.awesomechat.view.tablayoutfriend

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.awesomechat.R
import com.example.awesomechat.adapter.FriendAdapter
import com.example.awesomechat.databinding.FragmentFriendRequestBinding
import com.example.awesomechat.interact.InteractFriend
import com.example.awesomechat.interact.Swipe
import com.example.awesomechat.view.FragmentBase
import com.example.awesomechat.viewmodel.FriendViewModel
import com.example.awesomechat.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FriendRequestFragment : FragmentBase<FragmentFriendRequestBinding>() {
    @Inject
    lateinit var interactFriend: InteractFriend
    private lateinit var adapter: FriendAdapter
    private lateinit var adapterRequest: FriendAdapter
    private val viewModel: FriendViewModel by viewModels<FriendViewModel>()
    private val searchViewModel: SearchViewModel by activityViewModels<SearchViewModel>()

    override fun getFragmentView(): Int {
        return R.layout.fragment_friend_request
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FriendAdapter(interactFriend)
        adapterRequest = FriendAdapter(interactFriend)
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

