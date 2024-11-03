package com.example.awesomechat.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.awesomechat.R
import com.example.awesomechat.adapter.FragmentPageAdapter
import com.example.awesomechat.databinding.FragmentFriendBinding
import com.example.awesomechat.repository.firebase.FriendRepository
import com.example.awesomechat.viewmodel.FriendViewModel
import com.example.awesomechat.viewmodel.SearchViewModel
import com.example.awesomechat.viewmodel.factory.ViewModelFriendFactory
import com.example.awesomechat.viewmodel.factory.ViewModelSearchFactory
import com.google.android.material.tabs.TabLayoutMediator

class FragmentFriend : Fragment() {
    private var _binding: FragmentFriendBinding? = null
    private val binding get() = _binding!!
    private lateinit var customView:View
    private lateinit var adapter: FragmentPageAdapter
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var friendViewModel: FriendViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friend, container, false)
        adapter = FragmentPageAdapter(childFragmentManager, lifecycle)
        binding.viewPagerFriend.adapter = adapter
        binding.viewPagerFriend.offscreenPageLimit = 3
        val factory = ViewModelSearchFactory.getInstance()
        val factoryFriend = ViewModelFriendFactory(FriendRepository())
        searchViewModel = ViewModelProvider(this, factory)[SearchViewModel::class.java]
        friendViewModel = ViewModelProvider(this,factoryFriend)[FriendViewModel::class.java]
        TabLayoutMediator(binding.tabLayoutFriend, binding.viewPagerFriend) { tab, positions ->
            when(positions){
                0->tab.text="Bạn bè"
                1 ->tab.text= "Tất cả"
                2 ->tab.text= "Yêu cầu"
            }
        }.attach()
        customView = LayoutInflater.from(requireContext()).inflate(R.layout.tab_request,null)
        binding.tabLayoutFriend.getTabAt(2)?.customView =customView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val badgeRequest=customView.findViewById<TextView>(R.id.quantityRequest)
        val notificationLayout: FrameLayout = view.findViewById(R.id.notification)
        friendViewModel.quantityRequest.observe(viewLifecycleOwner){
            if(it==0){
                badgeRequest.visibility = View.GONE
                notificationLayout.visibility = View.GONE
            }
            else{
                badgeRequest.text=it.toString()
                badgeRequest.visibility = View.VISIBLE
                notificationLayout.visibility = View.VISIBLE
            }
        }
        binding.searchFriend.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    searchViewModel.setSearchQuery(newText)
                }
                return true
            }
        })
    }

}