package com.example.awesomechat.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.awesomechat.view.tablayoutfriend.FriendAllFragment
import com.example.awesomechat.view.tablayoutfriend.FriendCurrentFragment
import com.example.awesomechat.view.tablayoutfriend.FriendRequestFragment

class FragmentPageAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> FriendCurrentFragment()
            1 -> FriendAllFragment()
            2 -> FriendRequestFragment()
            else -> FriendCurrentFragment()
        }
    }

}