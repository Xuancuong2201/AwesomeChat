package com.example.awesomechat.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.awesomechat.R
import dagger.hilt.android.AndroidEntryPoint


abstract class FragmentBase<T : ViewDataBinding> : Fragment() {
    lateinit var binding: T
    lateinit var controllerRoot: NavController
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, getFragmentView(), container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        controllerRoot = (activity as MainActivity).findNavController(R.id.fragment)
        return binding.root
    }

    fun<VM:ViewModel> setViewModelForBinding(viewmodel:VM){
        try {
            binding.setVariable(BR.viewmodel, viewmodel)
        } catch (e: Exception) {
            throw IllegalStateException("Ensure your layout XML defines a variable named 'viewmodel'")
        }
    }

    abstract fun getFragmentView(): Int

}