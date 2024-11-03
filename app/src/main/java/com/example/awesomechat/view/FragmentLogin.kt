package com.example.awesomechat.view


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.awesomechat.R
import com.example.awesomechat.databinding.FragmentLoginBinding
import com.example.awesomechat.interact.DialogConfirm
import com.example.awesomechat.interact.InteractData.Companion.isValidEmail
import com.example.awesomechat.interact.InteractData.Companion.isValidPassword
import com.example.awesomechat.repository.firebase.AuthenticationRepository
import com.example.awesomechat.repository.firebase.UserRepository
import com.example.awesomechat.viewmodel.LoginViewmodel
import com.example.awesomechat.viewmodel.factory.ViewModelLoginFactory



class FragmentLogin : Fragment() {
    private var _binding:FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: LoginViewmodel
    private lateinit var controller : NavController
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        val factory = ViewModelLoginFactory(AuthenticationRepository(),UserRepository(),requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[LoginViewmodel::class.java]
        binding.viewmodel = viewModel
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        controller= findNavController()
        binding.tvSignUp.setOnClickListener{ controller.navigate(R.id.go_to_signUpFragmentz) }
        viewModel.email.observe(viewLifecycleOwner){
            viewModel.checkEnable()
            if (it.isEmpty()) {
                binding.edtEmail.error = getString(R.string.error_user_null)
            }
            else if (!isValidEmail(it)) {
                binding.edtEmail.error = getString(R.string.error_email_isValid)
            }
        }
        viewModel.password.observe(viewLifecycleOwner){
            viewModel.checkEnable()
            if (!isValidPassword(it)) {
                binding.edtPassword.error = getString(R.string.error_password_isValid)
            }
        }
        viewModel.stateLogin.observe(viewLifecycleOwner){
            when(it){
               true-> {controller.navigate(R.id.go_to_homeFragment)
                        this.onDetach() }
               false-> {val dialog = DialogConfirm.newInstance(getString(R.string.login_fail))
                    dialog.show(childFragmentManager,"Dialog_Confirm")}
                null-> Log.e("Tt","OK")
            }
        }
    }


}