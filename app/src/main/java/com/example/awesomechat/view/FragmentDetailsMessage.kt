package com.example.awesomechat.view

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentDetailsMessage : Fragment(), ImageFromGalleryAdapter.ImageClickInterface {
    private lateinit var binding: FragmentDetailsMessageBinding
    private lateinit var detailsMessageAdapter: DetailsMessageAdapter
    private lateinit var imageFromGalleryAdapter: ImageFromGalleryAdapter
    private lateinit var controller: NavController
    private lateinit var message: Messages
    private val argsNav: FragmentDetailsMessageArgs by navArgs()
    private val viewModel: DetailsMessageViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val argsNotify = arguments?.getSerializable(InfoFieldQuery.KEY_DETAILS) as Messages?
        message = argsNotify ?: argsNav.message
        viewModel.getDetailsMessage(message.email.toString())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        detailsMessageAdapter = DetailsMessageAdapter(viewModel)
        imageFromGalleryAdapter = ImageFromGalleryAdapter(this)
        binding = FragmentDetailsMessageBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        return binding.root
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
//            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(
//                    requireActivity(),
//                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//                    InfoFieldQuery.REQUEST_GALLERY
//                )
//            }
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        InfoFieldQuery.REQUEST_GALLERY
                    )
                }
            }
            imageFromGalleryAdapter.submitList(getListImageFromGallery())
        }

        binding.btSentImage.setOnClickListener {
            binding.btSentImage.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                viewModel.apply {
                    sentImage(message.email.toString(),getString(R.string.type_image))
                    clearListImage()
                    binding.progressBar.visibility = View.GONE
                    getDetailsMessage(message.email.toString())
                }
            }
        }
    }

    private fun getListImageFromGallery(): MutableList<String> {
        val imageList = mutableListOf<String>()
        val projection: Array<String> = arrayOf(MediaStore.Images.Media._ID)
        var mCursor: Cursor? = null
        try {
            mCursor = requireActivity().contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null
            )
            mCursor?.apply {
                val index = getColumnIndex(MediaStore.Images.Media._ID)
                while (moveToNext()) {
                    val id = getLong(index)
                    val contentUri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    imageList.add(contentUri.toString())

                }
            }
        } finally {
            mCursor?.close()
        }
        return imageList
    }

    override fun selectImage(position: Int, uri: String) {
        viewModel.selectImage(uri)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            InfoFieldQuery.REQUEST_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    imageFromGalleryAdapter.submitList(getListImageFromGallery())
                } else
                    Toast.makeText(context, getString(R.string.request_gallery), Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

}
