package com.example.awesomechat.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.awesomechat.R
import com.example.awesomechat.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHome : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var navHostFragment: NavHostFragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navHostFragment =
            childFragmentManager.findFragmentById(R.id.fragment_home) as NavHostFragment
        NavigationUI.setupWithNavController(binding.bottomNav, navHostFragment.navController)

        binding.cardview.viewTreeObserver.addOnGlobalLayoutListener {
            val insets = ViewCompat.getRootWindowInsets(view)
            val imeVisible = insets?.isVisible(WindowInsetsCompat.Type.ime())
            if (imeVisible != null && imeVisible == true)
                binding.cardview.visibility = View.GONE
            else
                binding.cardview.visibility = View.VISIBLE
        }

    }
}
