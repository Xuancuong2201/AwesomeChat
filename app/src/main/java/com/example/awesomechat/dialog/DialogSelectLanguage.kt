package com.example.awesomechat.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.DialogFragment
import com.example.awesomechat.databinding.DialogSelectLanguageBinding
import com.example.awesomechat.interact.InfoFieldQuery
import com.example.awesomechat.view.MainActivity
import java.util.Locale

class DialogSelectLanguage : DialogFragment() {
    private lateinit var dialogBinding: DialogSelectLanguageBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var mainActivity: MainActivity
    private var myLang = ""
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogBinding = DialogSelectLanguageBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        mainActivity = requireActivity() as MainActivity
        sharedPref = mainActivity.sharedPref
        myLang = mainActivity.sharedPref.getString(InfoFieldQuery.LANGUAGE, InfoFieldQuery.VIETNAM)!!
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
        if (myLang.contentEquals(InfoFieldQuery.VIETNAM)) {
            dialogBinding.rdbVi.isChecked = true
            dialogBinding.rdbEn.isChecked = false
        } else {
            dialogBinding.rdbVi.isChecked = false
            dialogBinding.rdbEn.isChecked = true
        }
        dialogBinding.btConfirm.setOnClickListener {
            if (dialogBinding.rdbEn.isChecked)
                setLanguage(InfoFieldQuery.ENGLISH)
            else
                setLanguage(InfoFieldQuery.VIETNAM)
            dialog?.dismiss()
            requireActivity().recreate()

        }
        dialogBinding.btCancel.setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun setLanguage(lang: String) {
        val config = resources.configuration
        val locale = Locale(lang)
        Locale.setDefault(locale)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        if (lang == InfoFieldQuery.VIETNAM) {
            with(sharedPref.edit()) {
                putString(InfoFieldQuery.LANGUAGE, InfoFieldQuery.VIETNAM)
                commit()
            }
        } else {
            with(sharedPref.edit()) {
                putString(InfoFieldQuery.LANGUAGE, InfoFieldQuery.ENGLISH)
                commit()
            }
        }
    }
    companion object {
        fun newInstance(): DialogSelectLanguage {
            return DialogSelectLanguage()
        }
    }
}