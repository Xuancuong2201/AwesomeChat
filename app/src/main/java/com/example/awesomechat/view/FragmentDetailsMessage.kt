package com.example.awesomechat.view

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

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
import com.example.awesomechat.interact.InteractData.Companion.adjustList
import com.example.awesomechat.viewmodel.DetailsMessageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentDetailsMessage : Fragment(), ImageFromGalleryAdapter.ImageClickInterface {
    private val args: FragmentDetailsMessageArgs by navArgs()
    private val viewModel: DetailsMessageViewModel by activityViewModels()
    private var _binding: FragmentDetailsMessageBinding? = null
    private val binding get() = _binding!!
    private lateinit var detailsMessageAdapter: DetailsMessageAdapter
    private lateinit var imageFromGalleryAdapter: ImageFromGalleryAdapter
    private lateinit var controller: NavController
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_details_message, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.getDetailsMessage(args.message.email.toString())
        binding.viewmodel = viewModel
        detailsMessageAdapter = DetailsMessageAdapter(viewModel)
        imageFromGalleryAdapter = ImageFromGalleryAdapter(this)
        imageFromGalleryAdapter.submitList(getListImageFromGallery())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller = findNavController()

        viewModel.imageUrl.value = args.message.url.toString()

        viewModel.name.value = args.message.name.toString()

        viewModel.content.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                viewModel.stateButton.value = false
                binding.btnSend.visibility = View.VISIBLE
                binding.rcvGallery.visibility = View.GONE
            } else
                binding.btnSend.visibility = View.GONE

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
            if (it.isNotEmpty()){
                detailsMessageAdapter.updateList(adjustList(it))
                binding.tvStatus.visibility=View.GONE
                binding.imgPersonal.visibility=View.GONE
                binding.rcvDetailsMessage.let { rcv ->
                    rcv.layoutManager = LinearLayoutManager(requireContext())
                    rcv.adapter = detailsMessageAdapter
                    rcv.scrollToPosition(it.size - 1)
            } }
            else{
                viewModel.createConversation(args.message.email.toString())
                binding.tvStatus.visibility=View.VISIBLE
                binding.imgPersonal.visibility=View.VISIBLE
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
        }

//        binding.rcvDetailsMessage.viewTreeObserver.addOnGlobalLayoutListener {
//            binding.rcvDetailsMessage.smoothScrollToPosition(detailsMessageAdapter.itemCount - 1)
//        }

        binding.btnSend.setOnClickListener {
            if (binding.tvMessage.text.toString().isNotEmpty()) {
                viewModel.sentMessage(args.message.email.toString())
                viewModel.getDetailsMessage(args.message.email.toString())
                binding.tvMessage.text.clear()
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
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        1
                    )
                } else
                    imageFromGalleryAdapter.submitList(getListImageFromGallery())

        }

        binding.btSentImage.setOnClickListener {
            lifecycleScope.launch {
                viewModel.apply {
                    sentImage(args.message.email.toString())
                    getDetailsMessage(args.message.email.toString())
                    clearListImage()
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
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
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
}
