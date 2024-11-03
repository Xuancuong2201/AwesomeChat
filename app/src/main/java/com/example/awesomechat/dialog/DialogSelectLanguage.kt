package com.example.awesomechat.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.DialogFragment
import com.example.awesomechat.databinding.DialogSelectLanguageBinding

class DialogSelectLanguage : DialogFragment() {
    private lateinit var dialogBinding: DialogSelectLanguageBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogBinding = DialogSelectLanguageBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogBinding.root)
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            it.window?.setGravity(Gravity.CENTER)
            it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setCancelable(false)
        }
        dialogBinding.btCancel.setOnClickListener {
            dialog?.dismiss()
        }
    }
    companion object {
        fun newInstance():DialogSelectLanguage {
            return DialogSelectLanguage()
        }
    }
}