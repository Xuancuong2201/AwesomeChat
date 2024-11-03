package com.example.awesomechat.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.awesomechat.MainActivity
import com.example.awesomechat.R
import com.example.awesomechat.databinding.FragmentPageBinding
import com.example.awesomechat.dialog.DialogSelectLanguage
import com.example.awesomechat.viewmodel.PageViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentPage : Fragment() {
    private var _binding: FragmentPageBinding? = null
    private  val viewModelPage: PageViewModel by activityViewModels()
    private lateinit var controller: NavController
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_page, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModelPage
        controller = findNavController()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogout.setOnClickListener {
            viewModelPage.logOut()
            val intent = Intent(requireActivity(),MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            onDetach()
        }
        binding.btEdit.setOnClickListener {
            val navController = (activity as MainActivity).findNavController(R.id.fragment)
           navController.navigate(R.id.action_homeFragment_to_fragmentEdit2)
        }

        binding.btnSelectLanguage.setOnClickListener{
            val dialog = DialogSelectLanguage.newInstance()
            dialog.show(parentFragmentManager,"dialog select language")
        }
    }

    override fun onStart() {
        super.onStart()
        viewModelPage.getUrlAndName()
    }
}