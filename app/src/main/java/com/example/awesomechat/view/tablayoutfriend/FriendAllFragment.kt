package com.example.awesomechat.view.tablayoutfriend

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.awesomechat.R
import com.example.awesomechat.adapter.FriendAdapter
import com.example.awesomechat.databinding.FragmentFriendAllBinding
import com.example.awesomechat.interact.InteractData
import com.example.awesomechat.interact.InteractFriend
import com.example.awesomechat.view.FragmentBase
import com.example.awesomechat.viewmodel.FriendViewModel
import com.example.awesomechat.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FriendAllFragment : FragmentBase<FragmentFriendAllBinding>() {
    @Inject
    lateinit var interactFriend: InteractFriend
    private lateinit var adapter: FriendAdapter
    private val viewModel: FriendViewModel by viewModels<FriendViewModel>()
    private val searchViewModel: SearchViewModel by activityViewModels<SearchViewModel>()

    override fun getFragmentView(): Int {
        return R.layout.fragment_friend_all
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FriendAdapter(interactFriend)
        adapter.submitList(emptyList())
        viewModel.combineValues().observe(viewLifecycleOwner) {
            adapter.submitList(it?.let { InteractData.convertToList(it) } ?: emptyList())
            binding.progressBar.visibility = View.GONE
            adapter.setItems(InteractData.convertToList(it))
            binding.rcvAllFriend.let { rcv ->
                rcv.layoutManager = LinearLayoutManager(requireContext())
                rcv.adapter = adapter
            }
        }
        searchViewModel.searchQuery.observe(viewLifecycleOwner) {
            adapter.filter.filter(it)
        }
    }
}

