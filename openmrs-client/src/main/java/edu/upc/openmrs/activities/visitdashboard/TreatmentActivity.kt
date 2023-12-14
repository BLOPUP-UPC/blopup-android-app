package edu.upc.openmrs.activities.visitdashboard

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.app.NavUtils
import androidx.core.content.res.ResourcesCompat
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
        setContentView(mBinding.root)
        intent.extras?.let { viewModel.treatment.value?.visitId = it.getLong(VISIT_ID) }

        setToolbar()
        whoRecommendedButtonsOnClickListener()
        registerTreatmentOnClickListener()
        treatmentObserver()
    }

    private fun setToolbar() {
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            elevation = 0f

            setTitle(R.string.add_treatment)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun treatmentObserver() {
        viewModel.treatment.observe(this) {
            when (it.recommendedBy) {
                RECOMMENDED_BY_BLOPUP -> {
                    mBinding.newRecommendation.background = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.treatment_buttons_on_selected,
                        null
                    )
                    mBinding.previouslyRecommended.background = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.treatment_buttons_on_unselected,
                        null
                    )
                }
                RECOMMENDED_BY_OTHER -> {
                    mBinding.previouslyRecommended.background = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.treatment_buttons_on_selected,
                        null
                    )
                    mBinding.newRecommendation.background = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.treatment_buttons_on_unselected,
                        null
                    )
                }
            }
        }
    }

    private fun fillTreatmentFields() {
        with(mBinding) {
            viewModel.treatment.value?.medicationName = medicationName.text.toString()
            viewModel.treatment.value?.notes = additionalNotes.text.toString()
            viewModel.treatment.value?.medicationType = medicationType.checkedChipIds
                .map {
                    MedicationType.valueOf(findViewById<Chip>(it).resources.getResourceName(it).split("/")[1].uppercase())
                }
                .toSet()
        }
    }

    private fun whoRecommendedButtonsOnClickListener() {
        mBinding.previouslyRecommended.setOnClickListener {
            viewModel.treatment.value = viewModel.treatment.value!!.apply { recommendedBy = RECOMMENDED_BY_OTHER }
        }
        mBinding.newRecommendation.setOnClickListener {
            viewModel.treatment.value = viewModel.treatment.value!!.apply { recommendedBy = RECOMMENDED_BY_BLOPUP }
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
