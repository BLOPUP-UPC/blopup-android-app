package edu.upc.openmrs.activities.visitdashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        return bloodPressureInfoBinding.root
    }
}