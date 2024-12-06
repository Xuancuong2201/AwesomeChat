package com.example.awesomechat.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.awesomechat.R
import com.example.awesomechat.databinding.FragmentSignUpBinding
import com.example.awesomechat.dialog.DialogConfirm
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractData.Companion.containsNumber
import com.example.awesomechat.interact.InteractData.Companion.isValidEmail
import com.example.awesomechat.interact.InteractData.Companion.isValidPassword
import com.example.awesomechat.viewmodel.SignUpViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentSignUp : FragmentBase<FragmentSignUpBinding>() {
    private val viewModel: SignUpViewModel by viewModels()

    override fun getFragmentView(): Int = R.layout.fragment_sign_up

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewModelForBinding(viewModel)

        binding.btnBack.setOnClickListener {
            controllerRoot.popBackStack()
        }
        binding.tvLogin.setOnClickListener {
            controllerRoot.popBackStack()

        }

        binding.btnRegister.setOnClickListener {
            if (viewModel.result.value == true) {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvStatus.text = getString(R.string.please_wait)
                viewModel.register()
            }
        }

        viewModel.account.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.edtName.error = getString(R.string.error_user_null)
            } else if (containsNumber(it)) {
                binding.edtName.error = getString(R.string.error_user_number)
            }
        }
        viewModel.email.observe(viewLifecycleOwner) {

            if (it.isEmpty()) {
                binding.edtEmail.error = getString(R.string.error_user_null)
            } else if (!isValidEmail(it)) {
                binding.edtEmail.error = getString(R.string.error_email_isValid)
            }
        }
        viewModel.password.observe(viewLifecycleOwner) {
            if (!isValidPassword(it)) {
                binding.edtPassword.error = getString(R.string.error_password_isValid)
            }
        }

        viewModel.stateRegister.observe(viewLifecycleOwner) {
            if (it == true) controllerRoot.navigate(R.id.go_to_homeFragment)
            else {
                val dialog = DialogConfirm.newInstance(getString(R.string.signup_fail))
                binding.progressBar.visibility = View.GONE
                binding.tvStatus.text = getString(R.string.signup1)
                dialog.show(childFragmentManager, InfoFieldQuery.DIALOG_CONFIRM)
            }
        }


    }
}