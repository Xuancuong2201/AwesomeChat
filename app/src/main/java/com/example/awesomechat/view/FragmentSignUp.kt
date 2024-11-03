package com.example.awesomechat.view

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.awesomechat.R
import com.example.awesomechat.databinding.FragmentSignUpBinding
import com.example.awesomechat.interact.DialogConfirm
import com.example.awesomechat.interact.InteractData.Companion.containsNumber
import com.example.awesomechat.interact.InteractData.Companion.isValidEmail
import com.example.awesomechat.interact.InteractData.Companion.isValidPassword
import com.example.awesomechat.viewmodel.SignUpViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentSignUp : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val  viewModel: SignUpViewModel by  activityViewModels()
    private lateinit var controller: NavController
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)      {
        super.onViewCreated(view, savedInstanceState)
        controller = findNavController()
        binding.btnBack.setOnClickListener {
            controller.navigate(R.id.action_signUpFragment_to_loginFragment)
        }
        binding.tvLogin.setOnClickListener {
            controller.navigate(R.id.action_signUpFragment_to_loginFragment)

        }
        viewModel.account.observe(viewLifecycleOwner){
            viewModel.checkEnable()
            if (it.isEmpty()) {
                binding.edtName.error = getString(R.string.error_user_null)
            } else if (containsNumber(it)) {
                binding.edtName.error = getString(R.string.error_user_number)
            }
        }
        viewModel.email.observe(viewLifecycleOwner){
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
        viewModel.isChecked.observe(viewLifecycleOwner){
            viewModel.checkEnable()
        }
        viewModel.stateRegister.observe(viewLifecycleOwner){
            if(it ==true) controller.navigate(R.id.go_to_homeFragment) else {
                val dialog = DialogConfirm.newInstance(getString(R.string.signup_fail))
                dialog.show(childFragmentManager,"Dialog_Confirm")
            }
        }
    }
}