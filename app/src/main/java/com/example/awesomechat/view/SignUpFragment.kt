package com.example.awesomechat.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.awesomechat.R
import com.example.awesomechat.databinding.FragmentSignUpBinding
import com.example.awesomechat.repository.AuthenticationRepository
import com.example.awesomechat.viewmodel.MyViewModelFactory
import com.example.awesomechat.viewmodel.SignUpViewModel
import com.google.firebase.auth.FirebaseAuth

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SignUpViewModel
    private lateinit var controller: NavController
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        var repository = AuthenticationRepository(requireContext())
        viewModel = ViewModelProvider(this, MyViewModelFactory(repository)).get(SignUpViewModel::class.java)
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)      {
        super.onViewCreated(view, savedInstanceState)
        controller = findNavController()
        binding.btnBack.setOnClickListener {
            controller.navigate(R.id.go_to_loginFragment)
        }
        binding.tvLogin.setOnClickListener {
            controller.navigate(R.id.go_to_loginFragment)
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        viewModel.account.observe(this, Observer {
            if (it.isEmpty()) {
                binding.edtName.setError(getString(R.string.error_user_null))
            } else if (viewModel.containsNumber(it)) {
                binding.edtName.setError(getString(R.string.error_user_number))
            }
        })
        viewModel.email.observe(this, Observer {
            if (it.isEmpty()) {
                binding.edtEmail.setError(getString(R.string.error_user_null))
            } else if (!viewModel.isValidEmail(it)) {
                binding.edtEmail.setError(getString(R.string.error_email_isValid))
            }
        })
        viewModel.password.observe(this, Observer {
            if (it.isEmpty() || it.length < 8 || !viewModel.isValidPassword(it)) {
                binding.edtPassword.setError(getString(R.string.error_password_isValid))
            }
        })
    }

}