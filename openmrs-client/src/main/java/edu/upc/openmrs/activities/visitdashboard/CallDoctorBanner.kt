package edu.upc.openmrs.activities.visitdashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.fragment.app.DialogFragment
import edu.upc.R
import edu.upc.databinding.CallDoctorBannerBinding

class CallDoctorBanner : DialogFragment() {
    private lateinit var callDoctorBinding: CallDoctorBannerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        callDoctorBinding = CallDoctorBannerBinding.inflate(inflater, container, false)
        return callDoctorBinding.root
    }

    override fun onStart() {
        super.onStart()
        setListeners()
        formatDialog()
    }

    override fun getTheme(): Int = R.style.NoMarginDialog

    private fun formatDialog() {
        val width = LayoutParams.MATCH_PARENT
        val height = LayoutParams.WRAP_CONTENT

        dialog!!.window!!.setLayout(width,height)
        dialog!!.window!!.setGravity(Gravity.TOP)

    }

    private fun setListeners() {
        callDoctorBinding.callButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:+34621039238")
            startActivity(intent)
            dismiss()
        }
        callDoctorBinding.cancelButton.setOnClickListener { dismiss() }
    }
}

