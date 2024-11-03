package com.example.awesomechat.view.tablayoutfriend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.awesomechat.R
import com.example.awesomechat.adapter.FriendAdapter
import com.example.awesomechat.databinding.FragmentFriendCurrenctBinding
import com.example.awesomechat.interact.InteractData
import com.example.awesomechat.repository.firebase.FriendRepository
import com.example.awesomechat.viewmodel.FriendViewModel
import com.example.awesomechat.viewmodel.SearchViewModel

import com.example.awesomechat.viewmodel.factory.ViewModelFriendFactory
import com.example.awesomechat.viewmodel.factory.ViewModelSearchFactory

class FriendCurrentFragment : Fragment() {
    private var _binding: FragmentFriendCurrenctBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FriendAdapter
    private lateinit var viewModel: FriendViewModel
    private lateinit var searchViewModel: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_friend_currenct, container, false)
        val factory = ViewModelFriendFactory(FriendRepository())
        val factorySearch = ViewModelSearchFactory.getInstance()
        viewModel = ViewModelProvider(this, factory)[FriendViewModel::class.java]
        searchViewModel = ViewModelProvider(this, factorySearch)[SearchViewModel::class.java]
        adapter=FriendAdapter(viewModel)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.friendList.observe(viewLifecycleOwner){ it ->
            adapter.updateList(InteractData.convertToList(it))
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

