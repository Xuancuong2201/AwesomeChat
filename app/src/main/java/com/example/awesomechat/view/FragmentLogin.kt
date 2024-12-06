package com.example.awesomechat.view


import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.example.awesomechat.R
import com.example.awesomechat.databinding.FragmentLoginBinding
import com.example.awesomechat.dialog.DialogConfirm
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractData.Companion.isValidEmail
import com.example.awesomechat.interact.InteractData.Companion.isValidPassword
import com.example.awesomechat.viewmodel.LoginViewmodel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentLogin : FragmentBase<FragmentLoginBinding>() {
    private val viewModel: LoginViewmodel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewModelForBinding(viewModel)
        binding.tvSignUp.setOnClickListener { controllerRoot.navigate(R.id.go_to_signUpFragmentz) }
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
        viewModel.stateLogin.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    controllerRoot.navigate(R.id.go_to_homeFragment)
                    this.onDetach()
                }

                false -> {
                    val dialog = DialogConfirm.newInstance(getString(R.string.login_fail))
                    binding.progressBar.visibility = View.GONE
                    binding.tvStatus.text = getString(R.string.signIn1)
                    dialog.show(childFragmentManager, InfoFieldQuery.DIALOG_CONFIRM)
                }

                null -> Log.e("Error", "OK")
            }
        }

        binding.btnLogin.setOnClickListener {
            if (viewModel.result.value == true) {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvStatus.text = getString(R.string.please_wait)
                viewModel.login()
            }
        }
    }

    override fun getFragmentView(): Int = R.layout.fragment_login
}