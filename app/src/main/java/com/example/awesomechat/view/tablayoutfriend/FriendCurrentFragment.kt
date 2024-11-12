package com.example.awesomechat.view.tablayoutfriend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.awesomechat.adapter.FriendAdapter
import com.example.awesomechat.databinding.FragmentFriendCurrenctBinding
import com.example.awesomechat.interact.InteractData
import com.example.awesomechat.viewmodel.FriendViewModel
import com.example.awesomechat.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendCurrentFragment : Fragment() {
    private lateinit var binding: FragmentFriendCurrenctBinding
    private lateinit var adapter: FriendAdapter
    private val viewModel: FriendViewModel by viewModels<FriendViewModel>()
    private val searchViewModel: SearchViewModel by activityViewModels<SearchViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendCurrenctBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = FriendAdapter(viewModel)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.friendList.observe(viewLifecycleOwner) {
            adapter.setItems(it?.let { InteractData.convertToList(it) } ?: emptyList())
            binding.progressBar.visibility = View.GONE
            binding.rcvCurrentFriend.let { rcv ->
                rcv.layoutManager = LinearLayoutManager(requireContext())
                rcv.adapter = adapter
            }
        }
        searchViewModel.searchQuery.observe(viewLifecycleOwner) {
            adapter.filter.filter(it)
        }
    }
}

