package com.example.awesomechat.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.awesomechat.R
import com.example.awesomechat.view.MainActivity

class DialogLogout(private val callBack: (Boolean) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.notification))
            .setMessage(getString(R.string.question_confirm))
            .setPositiveButton("OK") { dialog, _ ->
                callBack(true)
                dialog?.dismiss()
                startActivity(Intent(requireActivity(), MainActivity::class.java))
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            it.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            it.window?.setGravity(Gravity.CENTER)
            it.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            it.setCancelable(false)
        }
    }

    companion object {
        fun newInstance(callBack: (Boolean) -> Unit): DialogLogout {
            return DialogLogout(callBack)
        }
    }
}