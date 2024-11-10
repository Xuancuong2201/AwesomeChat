package com.example.awesomechat.view.tablayoutfriend

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.awesomechat.R
import com.example.awesomechat.adapter.FriendAdapter
import com.example.awesomechat.databinding.FragmentFriendAllBinding
import com.example.awesomechat.interact.InteractData
import com.example.awesomechat.viewmodel.FriendViewModel
import com.example.awesomechat.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendAllFragment : Fragment() {
    private var _binding: FragmentFriendAllBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FriendViewModel by activityViewModels()
    private val searchViewModel: SearchViewModel by activityViewModels()
    private lateinit var adapter: FriendAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_friend_all, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FriendAdapter(viewModel)
        adapter.submitList(emptyList())
        viewModel.combineValues().observe(viewLifecycleOwner) {
            adapter.submitList(it?.let { InteractData.convertToList(it) } ?: emptyList())
            binding.progressBar.visibility = View.GONE
            adapter.submitList(InteractData.convertToList(it))
            binding.rcvAllFriend.let { rcv ->
                rcv.layoutManager = LinearLayoutManager(requireContext())
                rcv.adapter = adapter
            }
        }
        searchViewModel.searchQuery.observe(viewLifecycleOwner) {
            if(it.isEmpty()){
                adapter.submitList(viewModel.combineValues().value)
            }
            else
            adapter.filter.filter(it)
        }
    }

    override fun onStart() {
        super.onStart()
    }


}

