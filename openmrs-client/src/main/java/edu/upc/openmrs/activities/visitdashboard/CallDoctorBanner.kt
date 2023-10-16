package edu.upc.openmrs.activities.visitdashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.upc.BuildConfig
import edu.upc.Secrets
import edu.upc.databinding.CallDoctorBannerBinding
import edu.upc.openmrs.utilities.SecretsUtils
import edu.upc.openmrs.utilities.makeGone

class CallDoctorBanner : Fragment() {
    private lateinit var callDoctorBinding: CallDoctorBannerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        callDoctorBinding = CallDoctorBannerBinding.inflate(inflater, container, false)
        setListeners()
        return callDoctorBinding.root
    }

    private fun setListeners() {
        callDoctorBinding.callButton.setOnClickListener {
            callDoctor()
            dismiss()
        }
        callDoctorBinding.cancelButton.setOnClickListener { dismiss() }
    }

    private fun callDoctor() {
        val intent = Intent(Intent.ACTION_CALL)
        val phoneNumber = SecretsUtils.getDoctorPhoneNumber()
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    private fun dismiss() {
        callDoctorBinding.callDoctorBanner.makeGone()
    }
}
