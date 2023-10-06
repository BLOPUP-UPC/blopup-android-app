package edu.upc.openmrs.activities.visitdashboard

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import edu.upc.databinding.BloodPressureInfoBinding

class BloodPressureInfoDialog : DialogFragment(){

    private lateinit var bloodPressureInfoBinding: BloodPressureInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bloodPressureInfoBinding = BloodPressureInfoBinding.inflate(inflater, container, false)

        bloodPressureInfoBinding.closeDialog.setOnClickListener {
            dismiss()
        }

        setBorderRadius()

        return bloodPressureInfoBinding.root
    }

    private fun setBorderRadius() {
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }
    }
}