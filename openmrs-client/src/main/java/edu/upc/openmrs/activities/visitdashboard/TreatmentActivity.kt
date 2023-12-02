package edu.upc.openmrs.activities.visitdashboard

import android.os.Bundle
import android.widget.RadioButton
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.databinding.TreatmentFormBinding
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.sdk.library.models.Treatment
import edu.upc.sdk.library.models.Treatment.Companion.RECOMMENDED_BY_BLOPUP
import edu.upc.sdk.library.models.Treatment.Companion.RECOMMENDED_BY_OTHER
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_ID
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TreatmentActivity : ACBaseActivity() {

    private lateinit var mBinding: TreatmentFormBinding
    private val viewModel: TreatmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = TreatmentFormBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        viewModel.treatment.visitId = savedInstanceState?.getString(VISIT_ID).toString()
        registerTreatmentOnClick()
    }

    private fun fillTreatmentFields() {
        with(mBinding) {
            viewModel.treatment.medicationName = medicationName.text.toString()
            viewModel.treatment.notes = additionalNotes.text.toString()
            viewModel.treatment.drugFamilies = medicationFamily.checkedChipIds
                .map { findViewById<Chip>(it).text.toString() }
                .toSet()
            val treatmentOrigin = findViewById<Chip>(treatmentOrigin.checkedRadioButtonId).text.toString()
            viewModel.treatment.recommendedBy = if (treatmentOrigin == "New recommendation")  RECOMMENDED_BY_BLOPUP else RECOMMENDED_BY_OTHER
            viewModel.treatment.isActive = treatmentOrigin != "Recommended but not taken"
        }
    }

    private fun registerTreatmentOnClick() {
        mBinding.registerMedication.setOnClickListener {
            fillTreatmentFields()
            lifecycleScope.launch { viewModel.registerTreatment() }
        }
    }
}