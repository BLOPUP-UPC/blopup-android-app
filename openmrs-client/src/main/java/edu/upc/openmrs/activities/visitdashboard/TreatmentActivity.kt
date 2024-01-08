package edu.upc.openmrs.activities.visitdashboard

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.databinding.TreatmentFormBinding
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.openmrs.activities.dialog.CustomFragmentDialog
import edu.upc.openmrs.activities.visitdashboard.TreatmentViewModel.Companion.MEDICATION_NAME
import edu.upc.openmrs.activities.visitdashboard.TreatmentViewModel.Companion.MEDICATION_TYPE
import edu.upc.openmrs.activities.visitdashboard.TreatmentViewModel.Companion.RECOMMENDED_BY
import edu.upc.openmrs.bundle.CustomDialogBundle
import edu.upc.sdk.library.models.MedicationType
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.Treatment
import edu.upc.sdk.library.models.Treatment.Companion.RECOMMENDED_BY_BLOPUP
import edu.upc.sdk.library.models.Treatment.Companion.RECOMMENDED_BY_OTHER
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_ID
import edu.upc.sdk.utilities.ToastUtil
import kotlinx.coroutines.launch
import java.net.UnknownHostException

private const val s = "medicationName"

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
        addOnBackPressedListener()
        addFieldValidationListeners()
        fieldValidationValueObserver()
    }

    private fun addOnBackPressedListener() {
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showDialogToConfirmExit()
                }
            }
        )
    }

    private fun showDialogToConfirmExit() {
        if (doWeHaveValues()) {
            CustomDialogBundle().apply {
                titleViewMessage = getString(R.string.treatment_discard_dialog_title)
                textViewMessage = getString(R.string.treatment_discard_dialog_body)
                rightButtonText = getString(R.string.treatment_discard_button_leave)
                leftButtonText = getString(R.string.treatment_discard_button_stay)
                leftButtonAction = CustomFragmentDialog.OnClickAction.DISMISS
                rightButtonAction = CustomFragmentDialog.OnClickAction.FINISH_ACTIVITY
            }.let {
                createAndShowDialog(it, "")
            }
        } else {
            finish()
        }
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
                showDialogToConfirmExit()
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

    private fun fieldValidationValueObserver() {
        viewModel.fieldValidation.observe(this) { validation ->
            mBinding.registerMedication.isEnabled = validation.values.all { it }
            mBinding.textInputLayoutMedicationName.error = getString(R.string.empty_value)
            mBinding.textInputLayoutMedicationName.isErrorEnabled = !validation[MEDICATION_NAME]!!
            if (validation[RECOMMENDED_BY]!!) { mBinding.recommendedByError.visibility = View.GONE }
            else { mBinding.recommendedByError.visibility = View.VISIBLE }
            if (validation[MEDICATION_TYPE]!!) { mBinding.medicationTypeError.visibility = View.GONE }
            else { mBinding.medicationTypeError.visibility = View.VISIBLE }
        }
    }

    private fun addFieldValidationListeners() = with(mBinding) {
        medicationName.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                viewModel.fieldValidation.value =
                    viewModel.fieldValidation.value?.apply { replace(MEDICATION_NAME, false) }
            } else {
                viewModel.fieldValidation.value =
                    viewModel.fieldValidation.value?.apply { replace(MEDICATION_NAME, true) }
            }
        }

        medicationType.children.forEach { chip ->
            chip.setOnClickListener {
                if (medicationType.checkedChipIds.isEmpty()) {
                    viewModel.fieldValidation.value =
                        viewModel.fieldValidation.value?.apply { replace(MEDICATION_TYPE, false) }
                } else {
                    viewModel.fieldValidation.value =
                        viewModel.fieldValidation.value?.apply { replace(MEDICATION_TYPE, true) }
                }
            }
        }
    }

    private fun whoRecommendedButtonsOnClickListener() {
        mBinding.previouslyRecommended.setOnClickListener {
            viewModel.treatment.value =
                viewModel.treatment.value!!.apply { recommendedBy = RECOMMENDED_BY_OTHER }
            viewModel.fieldValidation.value = viewModel.fieldValidation.value!!.apply {
                replace(RECOMMENDED_BY, true)
            }
        }

        mBinding.newRecommendation.setOnClickListener {
            viewModel.treatment.value =
                viewModel.treatment.value!!.apply { recommendedBy = RECOMMENDED_BY_BLOPUP }
            viewModel.fieldValidation.value = viewModel.fieldValidation.value?.apply {
                replace(RECOMMENDED_BY, true)
            }
        }
    }

    private fun registerTreatmentOnClickListener() {
        mBinding.registerMedication.setOnClickListener {
            fillTreatmentFields()
            lifecycleScope.launch {
                viewModel.registerTreatment()
            }

            with(viewModel.result) {
                this.observe(this@TreatmentActivity) {
                    handleTreatmentResult(it)
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
                    MedicationType.valueOf(
                        findViewById<Chip>(it).resources.getResourceName(it)
                            .split("/")[1].uppercase()
                    )
                }
                .toSet()
        }
    }

    private fun handleTreatmentResult(result: Result<Treatment>) =
        when {
            result is Result.Success -> {
                ToastUtil.success(getString(R.string.treatment_created_successfully))
                finish()
            }

            result is Result.Error && result.throwable.cause is UnknownHostException -> {
                ToastUtil.error(getString(R.string.no_internet_connection))
            }

            else -> {
                ToastUtil.error(getString(R.string.treatment_operation_error))
            }
        }

    private fun doWeHaveValues(): Boolean {
        return with(mBinding) {
            viewModel.treatment.value?.recommendedBy?.isNotEmpty() == true ||
                    medicationName.text.toString().isNotEmpty() ||
                    additionalNotes.text.toString().isNotEmpty() ||
                    medicationType.checkedChipIds.isNotEmpty()
        }
    }
}
