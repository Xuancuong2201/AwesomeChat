package com.example.awesomechat.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.awesomechat.R
import com.example.awesomechat.databinding.FragmentPageBinding
import com.example.awesomechat.dialog.DialogLogout
import com.example.awesomechat.dialog.DialogSelectLanguage
import com.example.awesomechat.viewmodel.PageViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentPage : Fragment() {
    private lateinit var binding: FragmentPageBinding
    private lateinit var controller: NavController
    private lateinit var mainActivity: MainActivity
    private val viewModelPage: PageViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = requireActivity() as MainActivity
        binding = FragmentPageBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModelPage
        controller = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogout.setOnClickListener {
            val dialog = DialogLogout.newInstance {
                if (it) viewModelPage.logOut()
            }
            dialog.show(childFragmentManager, "Dialog_Logout")
        }
        binding.btEdit.setOnClickListener {
            val navController = (activity as MainActivity).findNavController(R.id.fragment)
            navController.navigate(R.id.action_homeFragment_to_fragmentEdit2)
        }
        if (mainActivity.sharedPref.getString("language", "vi")!!.equals("vi"))
            binding.tvLanguageSelected.text = getString(R.string.vietnamese)
        else
            binding.tvLanguageSelected.text = getString(R.string.english)

        binding.btnSelectLanguage.setOnClickListener {
            val dialog = DialogSelectLanguage.newInstance()
            dialog.show(parentFragmentManager, "dialog select language")
        }
    }

    override fun onStart() {
        super.onStart()
        viewModelPage.getUrlAndName()
    }
}