package edu.upc.openmrs.activities.visitdashboard

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.model.Doctor
import edu.upc.blopup.model.MedicationType
import edu.upc.blopup.model.Treatment
import edu.upc.blopup.model.Treatment.Companion.RECOMMENDED_BY_BLOPUP
import edu.upc.blopup.model.Treatment.Companion.RECOMMENDED_BY_OTHER
import edu.upc.databinding.TreatmentFormBinding
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.openmrs.activities.dialog.CustomFragmentDialog
import edu.upc.openmrs.activities.visitdashboard.TreatmentViewModel.Companion.MEDICATION_NAME
import edu.upc.openmrs.activities.visitdashboard.TreatmentViewModel.Companion.MEDICATION_TYPE
import edu.upc.openmrs.activities.visitdashboard.TreatmentViewModel.Companion.RECOMMENDED_BY
import edu.upc.openmrs.bundle.CustomDialogBundle
import edu.upc.sdk.library.models.OperationType
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.TREATMENT
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_UUID
import edu.upc.sdk.utilities.ToastUtil
import kotlinx.coroutines.launch
import java.net.UnknownHostException

@AndroidEntryPoint
class TreatmentActivity : ACBaseActivity() {

    private lateinit var mBinding: TreatmentFormBinding
    private val viewModel: TreatmentViewModel by viewModels()

    private lateinit var treatmentToEdit: Treatment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = TreatmentFormBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        intent.extras?.let { viewModel.treatment.value?.visitUuid = it.getString(VISIT_UUID) }

        treatmentToEdit = intent.getParcelableExtra<Treatment>(TREATMENT)?.apply {
            viewModel.treatmentToEdit.value = this
        } ?: Treatment(
            recommendedBy = "",
            medicationName = "",
            medicationType = emptySet(),
        )

        if (treatmentToEdit.medicationName.isNotEmpty()) {
            viewModel.treatment.value?.recommendedBy = treatmentToEdit.recommendedBy
            viewModel.treatment.value?.visitUuid = treatmentToEdit.visitUuid
            completeFields(treatmentToEdit)
        }

        setToolbar()
        setDoctorWhoRecommendedDropDown()
        whoRecommendedButtonsOnClickListener()
        registerTreatmentOnClickListener()
        treatmentObserver()
        addOnBackPressedListener()
        setListenersForFieldValidation()
        fieldValidationValueObserver()

    }

    private fun setDoctorWhoRecommendedDropDown() {
        val adapter = ArrayAdapter<Doctor>(
            this,
            R.layout.doctors_name_dropdown
        )

        val dropDownWithDoctorsNames =
            findViewById<AutoCompleteTextView>(R.id.doctors_name_dropdown)
        dropDownWithDoctorsNames.setAdapter(adapter)

        viewModel.doctors.observe(this) { doctors ->
            adapter.clear()
            adapter.addAll(doctors)

            val doctorInfo = if(treatmentToEdit.doctorUuid?.isNotEmpty() == true) {
                getString(R.string.doctor_info, treatmentToEdit.doctorUuid, treatmentToEdit.doctorRegistrationNumber)
            } else {
                val doctor = doctors.firstOrNull()
                getString(R.string.doctor_info, doctor?.name, doctor?.registrationNumber)
            }
            dropDownWithDoctorsNames.setText(doctorInfo, false)
            dropDownWithDoctorsNames.tag = doctors.firstOrNull()?.uuid
        }

        lifecycleScope.launch { viewModel.getAllDoctors() }


        mBinding.doctorsNameDropdown.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val doctor = mBinding.doctorsNameDropdown.adapter.getItem(position) as Doctor
                dropDownWithDoctorsNames.tag = doctor.uuid
            }
    }

    private fun completeFields(treatmentToEdit: Treatment) {
        mBinding.medicationName.setText(treatmentToEdit.medicationName)

        if (treatmentToEdit.notes?.isNotEmpty() == true) {
            mBinding.additionalNotes.setText(treatmentToEdit.notes)
        }
        treatmentToEdit.medicationType.forEach {
            mBinding.medicationType.check(
                resources.getIdentifier(
                    it.name.lowercase(),
                    "id",
                    packageName
                )
            )
        }
        setRecommendationBackgrounds(treatmentToEdit.recommendedBy.trim())

        viewModel.updateFieldValidation(MEDICATION_NAME, true)
        viewModel.updateFieldValidation(MEDICATION_TYPE, true)
        viewModel.updateFieldValidation(RECOMMENDED_BY, true)
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
            setRecommendationBackgrounds(it.recommendedBy)
        }
    }

    private fun fieldValidationValueObserver() {
        viewModel.fieldValidation.observe(this) { isValid ->
            if (isValid.values.all { it }) {
                enableButton()
            } else {
                disableButton()
            }

            mBinding.textInputLayoutMedicationName.error = getString(R.string.empty_value)
            mBinding.textInputLayoutMedicationName.isErrorEnabled = (isValid[MEDICATION_NAME] ?: false).not()

            if (isValid[RECOMMENDED_BY] == true) {
                mBinding.recommendedByError.visibility = View.GONE
            } else {
                mBinding.recommendedByError.visibility = View.VISIBLE
            }

            if (isValid[MEDICATION_TYPE] == true) {
                mBinding.medicationTypeError.visibility = View.GONE
            } else {
                mBinding.medicationTypeError.visibility = View.VISIBLE
            }
        }
    }

    private fun enableButton() {
        mBinding.registerMedication.isEnabled = true
        mBinding.registerMedication.setBackgroundColor(
            resources.getColor(
                R.color.color_accent,
                null
            )
        )
    }

    private fun disableButton() {
        mBinding.registerMedication.isEnabled = false
        mBinding.registerMedication.setBackgroundColor(
            resources.getColor(
                R.color.dark_grey_for_stroke,
                null
            )
        )
    }

    private fun setListenersForFieldValidation() = with(mBinding) {
        medicationName.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                viewModel.updateFieldValidation(MEDICATION_NAME, false)
            } else {
                viewModel.updateFieldValidation(MEDICATION_NAME, true)
            }
        }

        medicationType.children.forEach { chip ->
            chip.setOnClickListener {
                if (medicationType.checkedChipIds.isEmpty()) {
                    viewModel.updateFieldValidation(MEDICATION_TYPE, false)
                } else {
                    viewModel.updateFieldValidation(MEDICATION_TYPE, true)
                }
            }
        }
    }

    private fun whoRecommendedButtonsOnClickListener() {
        mBinding.previouslyRecommended.setOnClickListener {

            viewModel.treatment.value =
                viewModel.treatment.value!!.apply { recommendedBy = RECOMMENDED_BY_OTHER }
            viewModel.updateFieldValidation(RECOMMENDED_BY, true)
        }

        mBinding.newRecommendation.setOnClickListener {

            viewModel.treatment.value =
                viewModel.treatment.value!!.apply { recommendedBy = RECOMMENDED_BY_BLOPUP }
            viewModel.updateFieldValidation(RECOMMENDED_BY, true)
        }
    }

    private fun registerTreatmentOnClickListener() {
        mBinding.registerMedication.setOnClickListener {

            disableButton()

            fillTreatmentFields()

            if (treatmentToEdit.medicationName.isNotEmpty()) {
                lifecycleScope.launch {
                    viewModel.updateTreatment()
                }
            } else {
                lifecycleScope.launch {
                    viewModel.registerTreatment()
                }
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
            if (additionalNotes.text.toString().isNotEmpty()) viewModel.treatment.value?.notes =
                additionalNotes.text.toString()
            viewModel.treatment.value?.medicationType = medicationType.checkedChipIds
                .map {
                    MedicationType.valueOf(
                        findViewById<Chip>(it).resources.getResourceName(it)
                            .split("/")[1].uppercase()
                    )
                }
                .toSet()
            if(viewModel.treatment.value?.recommendedBy == RECOMMENDED_BY_BLOPUP) {

                viewModel.treatment.value?.doctorUuid = doctorsNameDropdown.tag as String?
            }
        }
    }

    private fun handleTreatmentResult(result: Result<Treatment>) {
        when {
            result is Result.Success -> {
                if (result.operationType == OperationType.TreatmentUpdated) {
                    ToastUtil.success(getString(R.string.treatment_updated_successfully))
                } else {
                    ToastUtil.success(getString(R.string.treatment_created_successfully))
                }
                finish()
                return
            }

            result is Result.Error && result.throwable.cause is UnknownHostException -> {
                ToastUtil.error(getString(R.string.no_internet_connection))
            }

            else -> {
                ToastUtil.error(getString(R.string.treatment_operation_error))
            }
        }
        enableButton()
    }


    private fun doWeHaveValues(): Boolean {
        return with(mBinding) {
            viewModel.treatment.value?.recommendedBy?.isNotEmpty() == true ||
                    medicationName.text.toString().isNotEmpty() ||
                    additionalNotes.text.toString().isNotEmpty() ||
                    medicationType.checkedChipIds.isNotEmpty()
        }
    }

    private fun setRecommendationBackgrounds(recommendedBy: String) {
        when (recommendedBy) {
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
                mBinding.textInputLayoutDoctorsName.visibility = View.VISIBLE
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
                mBinding.textInputLayoutDoctorsName.visibility = View.GONE
            }
        }
    }
}
