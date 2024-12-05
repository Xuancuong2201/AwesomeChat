package com.example.awesomechat.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
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
    private lateinit var controller: NavController
    private val viewModel: SignUpViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun getFragmentView(): Int = R.layout.fragment_sign_up

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = findNavController()
        binding.btnBack.setOnClickListener {
            controller.navigate(R.id.action_signUpFragment_to_loginFragment)
        }
        binding.tvLogin.setOnClickListener {
            controller.navigate(R.id.action_signUpFragment_to_loginFragment)

        }
        viewModel.account.observe(viewLifecycleOwner) {
            viewModel.checkEnable()
            if (it.isEmpty()) {
                binding.edtName.error = getString(R.string.error_user_null)
            } else if (containsNumber(it)) {
                binding.edtName.error = getString(R.string.error_user_number)
            }
        }
        viewModel.email.observe(viewLifecycleOwner) {
            viewModel.checkEnable()
            if (it.isEmpty()) {
                binding.edtEmail.error = getString(R.string.error_user_null)
            } else if (!isValidEmail(it)) {
                binding.edtEmail.error = getString(R.string.error_email_isValid)
            }
        }
        viewModel.password.observe(viewLifecycleOwner) {
            viewModel.checkEnable()
            if (!isValidPassword(it)) {
                binding.edtPassword.error = getString(R.string.error_password_isValid)
            }
        }
        viewModel.isChecked.observe(viewLifecycleOwner) {
            viewModel.checkEnable()
        }
        viewModel.stateRegister.observe(viewLifecycleOwner) {
            if (it == true) controller.navigate(R.id.go_to_homeFragment)
            else {
                val dialog = DialogConfirm.newInstance(getString(R.string.signup_fail))
                binding.progressBar.visibility = View.GONE
                binding.tvStatus.text = getString(R.string.signup1)
                dialog.show(childFragmentManager, InfoFieldQuery.DIALOG_CONFIRM)
            }
        }
        viewModel.result.observe(viewLifecycleOwner) {
            val color = if (it) R.color.primary_color else R.color.no_focus
            binding.btnRegister.setBackgroundColor(ContextCompat.getColor(requireContext(), color))
        }
        binding.btnRegister.setOnClickListener {
            if (viewModel.result.value == true) {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvStatus.text = getString(R.string.please_wait)
                viewModel.register()
            }
        }
    }
}