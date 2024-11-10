package com.example.awesomechat.view.tablayoutfriend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.awesomechat.R
import com.example.awesomechat.adapter.FriendAdapter
import com.example.awesomechat.databinding.FragmentFriendCurrenctBinding
import com.example.awesomechat.interact.InteractData
import com.example.awesomechat.viewmodel.FriendViewModel
import com.example.awesomechat.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendCurrentFragment : Fragment() {
    private var _binding: FragmentFriendCurrenctBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FriendAdapter
    private val viewModel: FriendViewModel by activityViewModels()
    private val searchViewModel: SearchViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_friend_currenct, container, false)
        adapter = FriendAdapter(viewModel)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.friendList.observe(viewLifecycleOwner) {
            adapter.submitList(it?.let { InteractData.convertToList(it) } ?: emptyList())
            binding.progressBar.visibility = View.GONE
            binding.rcvCurrentFriend.let { rcv ->
                rcv.layoutManager = LinearLayoutManager(requireContext())
                rcv.adapter = adapter
            }
        }
        searchViewModel.searchQuery.observe(viewLifecycleOwner) {
            if(it.isEmpty()){
                adapter.submitList(viewModel.friendList.value)
            }
            else
            adapter.filter.filter(it)
        }
    }
}

