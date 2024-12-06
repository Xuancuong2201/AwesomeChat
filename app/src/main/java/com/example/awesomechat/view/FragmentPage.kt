package com.example.awesomechat.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.awesomechat.R
import com.example.awesomechat.databinding.FragmentPageBinding
import com.example.awesomechat.dialog.DialogLogout
import com.example.awesomechat.dialog.DialogSelectLanguage
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.viewmodel.PageViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentPage : FragmentBase<FragmentPageBinding>() {
    private val viewModelPage: PageViewModel by viewModels()
    override fun getFragmentView(): Int {
        return R.layout.fragment_page
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewModelForBinding(viewModelPage)
        binding.imgPersonalMini.setOnClickListener {
            val dialog = DialogLogout.newInstance {
                if (it) viewModelPage.logOut()
            }
            dialog.show(childFragmentManager, InfoFieldQuery.DIALOG_LOGOUT)
        }
        binding.btEdit.setOnClickListener {
            controllerRoot.navigate(R.id.action_homeFragment_to_fragmentEdit2)
        }
        if ((requireActivity() as MainActivity).sharedPref.getString(
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

    override fun onResume() {
        super.onResume()
        viewModelPage.updateUser()
    }
}