package com.example.awesomechat.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.awesomechat.R
import com.example.awesomechat.databinding.FragmentPageBinding
import com.example.awesomechat.dialog.DialogLogout
import com.example.awesomechat.dialog.DialogSelectLanguage
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.viewmodel.PageViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentPage : FragmentBase<FragmentPageBinding>() {
    private lateinit var controller: NavController
    private lateinit var mainActivity: MainActivity
    private val viewModelPage: PageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        mainActivity = requireActivity() as MainActivity
        binding.viewmodel = viewModelPage
        return binding.root
    }
    override fun getFragmentView(): Int {
        return R.layout.fragment_page
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = findNavController()
        binding.imgPersonalMini.setOnClickListener {
            val dialog = DialogLogout.newInstance {
                if (it) viewModelPage.logOut()
            }
            dialog.show(childFragmentManager, InfoFieldQuery.DIALOG_LOGOUT)
        }
        binding.btEdit.setOnClickListener {
            val navController = (activity as MainActivity).findNavController(R.id.fragment)
            navController.navigate(R.id.action_homeFragment_to_fragmentEdit2)
        }
        if (mainActivity.sharedPref.getString(
                InfoFieldQuery.LANGUAGE,
                InfoFieldQuery.VIETNAM
            )!! == InfoFieldQuery.VIETNAM
        )
            binding.tvLanguageSelected.text = getString(R.string.vietnamese)
        else
            binding.tvLanguageSelected.text = getString(R.string.english)

        binding.btnSelectLanguage.setOnClickListener {
            val dialog = DialogSelectLanguage.newInstance()
            dialog.show(parentFragmentManager, InfoFieldQuery.DIALOG_SELECT)
        }
    }
}