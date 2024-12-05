package com.example.awesomechat.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.awesomechat.R
import com.example.awesomechat.adapter.DetailsMessageAdapter
import com.example.awesomechat.adapter.ImageFromGalleryAdapter
import com.example.awesomechat.databinding.FragmentDetailsMessageBinding
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.interact.InteractData.Companion.adjustList
import com.example.awesomechat.model.Messages
import com.example.awesomechat.viewmodel.DetailsMessageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class FragmentDetailsMessage : FragmentBase<FragmentDetailsMessageBinding>(),
    ImageFromGalleryAdapter.ImageClickInterface {
    private lateinit var detailsMessageAdapter: DetailsMessageAdapter
    private lateinit var imageFromGalleryAdapter: ImageFromGalleryAdapter
    private lateinit var controller: NavController
    private lateinit var message: Messages
    private lateinit var listImage: List<String>
    private val argsNav: FragmentDetailsMessageArgs by navArgs()
    private val viewModel: DetailsMessageViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val argsNotify = arguments?.getSerializable(InfoFieldQuery.KEY_DETAILS) as Messages?
        message = argsNotify ?: argsNav.message
        viewModel.getDetailsMessage(message.email.toString())
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                listImage = viewModel.getListImageFromGallery()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        detailsMessageAdapter = DetailsMessageAdapter(message.url.toString()    )
        imageFromGalleryAdapter = ImageFromGalleryAdapter(this)
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun getFragmentView(): Int {
        return R.layout.fragment_details_message
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = findNavController()
        viewModel.imageUrl.value = message.url
        viewModel.name.value = message.name
        viewModel.email.value = message.email


        viewModel.content.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                viewModel.stateButton.value = false
                binding.btnSend.visibility = View.VISIBLE
                binding.rcvGallery.visibility = View.GONE
            } else binding.btnSend.visibility = View.GONE
        }
        viewModel.stateButton.observe(viewLifecycleOwner) {
            if (it) {
                binding.btnSend.visibility = View.VISIBLE
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.tvMessage.windowToken, 0)
                binding.tvMessage.clearFocus()
            } else {
                binding.btnSend.visibility = View.GONE
            }
        }
        viewModel.listDetailsMessage.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                detailsMessageAdapter.submitList(adjustList(it))
                binding.rcvDetailsMessage.let { rcv ->
                    rcv.layoutManager = LinearLayoutManager(requireContext())
                    rcv.adapter = detailsMessageAdapter
                    rcv.scrollToPosition(detailsMessageAdapter.itemCount - 1)
                }
            }
        }
        viewModel.listImageLiveData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.btSentImage.visibility = View.VISIBLE
            } else {
                binding.btSentImage.visibility = View.GONE
            }
        }

        binding.btnBack.setOnClickListener {
            controller.popBackStack()
            viewModel.listDetailsMessage.postValue(emptyList())
            this.onDestroy()

        }
        binding.btnSend.setOnClickListener {
            if (binding.tvMessage.text.toString().isNotEmpty()) {
                lifecycleScope.launch {
                    viewModel.sentMessage(
                        viewModel.email.value.toString(), binding.tvMessage.text.toString()
                    )
                    binding.tvMessage.text.clear()
                }
            }
        }

        binding.tvMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.stateButton.postValue(false)
            }
        }

        binding.rcvGallery.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = imageFromGalleryAdapter
        }

        binding.btGallery.setOnClickListener {
            viewModel.changeStateButton()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionStorage.launch(Manifest.permission.READ_MEDIA_IMAGES)
                } else
                    imageFromGalleryAdapter.submitList(listImage)
            }
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionStorage.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                imageFromGalleryAdapter.submitList(listImage)
            }
        }

        binding.btSentImage.setOnClickListener {
            binding.btSentImage.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                viewModel.apply {
                    sentImage(message.email.toString(), getString(R.string.type_image))
                    clearListImage()
                    binding.progressBar.visibility = View.GONE
                    getDetailsMessage(message.email.toString())
                }
            }
        }
    }


    override fun selectImage(position: Int, uri: String) {
        viewModel.selectImage(uri)
    }

    private val requestPermissionStorage =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                CoroutineScope(Dispatchers.IO).launch {
                    val newList = viewModel.getListImageFromGallery()
                    withContext(Dispatchers.Main){
                            imageFromGalleryAdapter.submitList(newList)
                    }
                }
            }
        }
}



