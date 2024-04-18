package edu.upc.openmrs.activities.visitdashboard

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import edu.upc.R
import edu.upc.databinding.CallDoctorBannerBinding
import edu.upc.openmrs.utilities.makeGone
import edu.upc.sdk.utilities.ToastUtil

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

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                callDoctor()
            } else {
                ToastUtil.showLongToast(requireContext(), ToastUtil.ToastType.WARNING, R.string.message_call_permission_denied)
            }
        }

    private fun setListeners() {
        callDoctorBinding.callButton.setOnClickListener {
            askForPermissionBeforeCallingTheDoctor()
        }
        callDoctorBinding.cancelButton.setOnClickListener { dismiss() }
    }

    private fun askForPermissionBeforeCallingTheDoctor() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED) {
                callDoctor()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
        }
    }

    private fun callDoctor() {
        dismiss()
        val intent = Intent(Intent.ACTION_CALL)
        val phoneNumber = getString(R.string.doctors_phone)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    private fun dismiss() {
        callDoctorBinding.callDoctorBanner.makeGone()
    }
}
