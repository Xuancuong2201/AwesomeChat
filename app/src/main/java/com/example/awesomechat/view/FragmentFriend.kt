package com.example.awesomechat.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.awesomechat.R
import com.example.awesomechat.adapter.FragmentPageAdapter
import com.example.awesomechat.databinding.FragmentFriendBinding
import com.example.awesomechat.viewmodel.FriendViewModel
import com.example.awesomechat.viewmodel.SearchViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentFriend : Fragment() {
    private lateinit var binding: FragmentFriendBinding
    private lateinit var customView: View
    private lateinit var adapter: FragmentPageAdapter
    private val searchViewModel: SearchViewModel by activityViewModels<SearchViewModel>()
    private val friendViewModel: FriendViewModel by viewModels<FriendViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendBinding.inflate(inflater)
        adapter = FragmentPageAdapter(childFragmentManager, lifecycle)
        binding.viewPagerFriend.adapter = adapter
        binding.viewPagerFriend.offscreenPageLimit = 3
        TabLayoutMediator(binding.tabLayoutFriend, binding.viewPagerFriend) { tab, positions ->
            when (positions) {
                0 -> tab.setText(R.string.friend)
                1 -> tab.setText(R.string.all)
                2 -> tab.setText(R.string.request)
            }
        }.attach()
        customView = LayoutInflater.from(requireContext()).inflate(R.layout.tab_request, null)
        binding.tabLayoutFriend.getTabAt(2)?.customView = customView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val badgeRequest = customView.findViewById<TextView>(R.id.quantityRequest)
        val notificationLayout: FrameLayout = view.findViewById(R.id.notification)
        friendViewModel.quantityRequest.observe(viewLifecycleOwner) {
            if (it == 0) {
                badgeRequest.visibility = View.GONE
                notificationLayout.visibility = View.GONE
            } else {
                badgeRequest.text = it.toString()
                badgeRequest.visibility = View.VISIBLE
                notificationLayout.visibility = View.VISIBLE
            }
        }

    }

    override fun onStart() {
        super.onStart()
        binding.searchFriend.setOnQueryTextListener(object :
            android.widget.SearchView.OnQueryTextListener {
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