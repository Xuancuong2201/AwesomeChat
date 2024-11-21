package com.example.awesomechat.view


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.awesomechat.R
import com.example.awesomechat.databinding.FragmentLoginBinding
import com.example.awesomechat.dialog.DialogConfirm
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractData.Companion.isValidEmail
import com.example.awesomechat.interact.InteractData.Companion.isValidPassword
import com.example.awesomechat.viewmodel.LoginViewmodel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentLogin : Fragment() {
    private val viewModel: LoginViewmodel by viewModels()
    private lateinit var binding: FragmentLoginBinding
    private lateinit var controller: NavController
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        controller = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvSignUp.setOnClickListener { controller.navigate(R.id.go_to_signUpFragmentz) }
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
        viewModel.stateLogin.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    controller.navigate(R.id.go_to_homeFragment)
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
        viewModel.result.observe(viewLifecycleOwner) {
            val color = if (it) R.color.primary_color else R.color.no_focus
            binding.btnLogin.setBackgroundColor(ContextCompat.getColor(requireContext(), color))
        }
        binding.btnLogin.setOnClickListener {
            if (viewModel.result.value == true) {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvStatus.text = getString(R.string.please_wait)
                viewModel.login()
            }
        }
    }


}