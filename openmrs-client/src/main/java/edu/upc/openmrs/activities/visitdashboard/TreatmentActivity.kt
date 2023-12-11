package edu.upc.openmrs.activities.visitdashboard

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.databinding.TreatmentFormBinding
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.sdk.library.models.MedicationType
import edu.upc.sdk.library.models.Treatment.Companion.RECOMMENDED_BY_BLOPUP
import edu.upc.sdk.library.models.Treatment.Companion.RECOMMENDED_BY_OTHER
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_ID
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TreatmentActivity : ACBaseActivity() {

    private lateinit var mBinding: TreatmentFormBinding
    private val viewModel: TreatmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = TreatmentFormBinding.inflate(layoutInflater)

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            elevation = 0f

            setTitle(R.string.register_treatment)
            setDisplayHomeAsUpEnabled(true)
        }

        setContentView(mBinding.root)
        intent.extras?.let { viewModel.treatment.visitId = it.getLong(VISIT_ID) }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        whoRecommendedButtonsOnClickListener()
        registerTreatmentOnClickListener()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    private fun fillTreatmentFields() {
        with(mBinding) {
            viewModel.treatment.medicationName = medicationName.text.toString()
            viewModel.treatment.notes = additionalNotes.text.toString()
            viewModel.treatment.medicationType = medicationType.checkedChipIds
                .map {
                    MedicationType.valueOf(findViewById<Chip>(it).resources.getResourceName(it).split("/")[1].uppercase())
                }
                .toSet()
        }
    }

    private fun whoRecommendedButtonsOnClickListener() {
        mBinding.previouslyRecommended.setOnClickListener {
            viewModel.treatment.recommendedBy = RECOMMENDED_BY_OTHER
            mBinding.previouslyRecommended.setBackgroundColor(
                resources.getColor(
                    R.color.light_grey_for_solid,
                    null
                )
            )
            mBinding.newRecommendation.setBackgroundColor(
                resources.getColor(
                    R.color.white,
                    null
                )
            )
        }
        mBinding.newRecommendation.setOnClickListener {
            viewModel.treatment.recommendedBy = RECOMMENDED_BY_BLOPUP
            mBinding.newRecommendation.setBackgroundColor(
                resources.getColor(
                    R.color.light_grey_for_solid,
                    null
                )
            )
            mBinding.previouslyRecommended.setBackgroundColor(
                resources.getColor(
                    R.color.white,
                    null
                )
            )
        }
    }

    private fun registerTreatmentOnClickListener() {
        mBinding.registerMedication.setOnClickListener {
            fillTreatmentFields()
            lifecycleScope.launch { viewModel.registerTreatment() }
            finish()
        }
    }
}
