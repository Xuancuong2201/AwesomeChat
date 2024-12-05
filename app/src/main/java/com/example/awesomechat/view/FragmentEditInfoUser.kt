package com.example.awesomechat.view

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.ext.SdkExtensions
import android.provider.MediaStore.ACTION_PICK_IMAGES
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.awesomechat.R
import com.example.awesomechat.databinding.FragmentEditInforUserBinding
import com.example.awesomechat.dialog.DialogConfirm
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractData
import com.example.awesomechat.viewmodel.EditViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentEditInfoUser : FragmentBase<FragmentEditInforUserBinding>() {

    private lateinit var controller: NavController
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val viewModel: EditViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding.viewmodel = viewModel
        controller = findNavController()
        openGallery()
        return binding.root
    }

    override fun getFragmentView(): Int {
        return R.layout.fragment_edit_infor_user
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener {
            controller.popBackStack()
        }
        binding.btSelectImg.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(
                    Build.VERSION_CODES.R
                ) >= 2
            ) {
                launcher.launch(Intent(ACTION_PICK_IMAGES))
            }
        }
        viewModel.name.observe(viewLifecycleOwner) {
            viewModel.checkEnable()
            if (it.isEmpty())
                binding.edtName.error = getString(R.string.error_user_null)
            else if (InteractData.containsNumber(it))
                binding.edtName.error = getString(R.string.error_user_number)
        }
        viewModel.numberPhone.observe(viewLifecycleOwner) {
            viewModel.checkEnable()
            if (it.isEmpty() || InteractData.isNumberPhone(it))
                binding.edtPhone.error = getString(R.string.error_numberphone)
        }
        viewModel.birthDay.observe(viewLifecycleOwner) {
            viewModel.checkEnable()
            if (it.isEmpty())
                binding.edtBirthday.error = getString(R.string.error_birthday_null)
            else if (!InteractData.isValidDateFormat(it.toString()))
                binding.edtBirthday.error = getString(R.string.error_birthday)
        }
        binding.btnSave.setOnClickListener {
            if (viewModel.result.value == true) {
                viewModel.updateRecordUser()
                controller.popBackStack()
            } else {
                val dialog = DialogConfirm.newInstance(getString(R.string.edit_fail))
                dialog.show(childFragmentManager, InfoFieldQuery.DIALOG_CONFIRM)
            }
        }
    }

    private fun openGallery() {
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    binding.imgPersonalMini.setImageURI(it.data?.data)
                    viewModel.url.postValue(it.data?.data.toString())
                }
            }
    }
}