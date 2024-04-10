/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package edu.upc.openmrs.activities.addeditpatient

import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.LinearLayout.LayoutParams
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.BuildConfig
import edu.upc.R
import edu.upc.databinding.FragmentPatientInfoBinding
import edu.upc.openmrs.activities.BaseFragment
import edu.upc.openmrs.activities.addeditpatient.countryofbirth.Country
import edu.upc.openmrs.activities.addeditpatient.countryofbirth.CountryOfBirthDialogFragment
import edu.upc.openmrs.activities.dialog.CustomFragmentDialog
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardActivity
import edu.upc.openmrs.listeners.watcher.DateOfBirthTextWatcher
import edu.upc.openmrs.utilities.ViewUtils.getInput
import edu.upc.openmrs.utilities.ViewUtils.isEmpty
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeVisible
import edu.upc.openmrs.utilities.observeOnce
import edu.upc.sdk.library.models.OperationType.PatientRegistering
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE
import edu.upc.sdk.utilities.DateUtils
import edu.upc.sdk.utilities.DateUtils.convertTime
import edu.upc.sdk.utilities.DateUtils.convertTimeString
import edu.upc.sdk.utilities.StringUtils.notEmpty
import edu.upc.sdk.utilities.StringUtils.notNull
import edu.upc.sdk.utilities.ToastUtil
import org.joda.time.LocalDate
import java.net.UnknownHostException
import java.util.Calendar


@AndroidEntryPoint
class AddEditPatientFragment : BaseFragment() {
    private var legalConsentDialog: LegalConsentDialogFragment? = null
    private var countryOfBirthDialogFragment: CountryOfBirthDialogFragment? = null
    private var patientCountry: Country? = null
    private var _binding: FragmentPatientInfoBinding? = null
    private val binding get() = _binding!!

    val viewModel: AddEditPatientViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPatientInfoBinding.inflate(inflater, container, false)
        val rootView = binding.root

        setHasOptionsMenu(true)

        setupObservers()

        setupViewsListeners()

        addListenersToAllFields()

        setCountrySpinner()

        fillFormFields()

        askPermissions()

        return rootView
    }

    private fun addBottomMargin() {
        val densityOperator = context?.resources?.displayMetrics?.density?.toInt()

        val bottomMargin = binding.linearLayoutCountryOfBirth.layoutParams as LayoutParams
        bottomMargin.bottomMargin = densityOperator!! * 90
        binding.linearLayoutCountryOfBirth.layoutParams = bottomMargin
    }

    private fun setCountrySpinner() {
        val countryOfBirthTextView = binding.countryOfBirth

        countryOfBirthTextView.setOnClickListener {
            // Initialize dialog
            countryOfBirthDialogFragment = CountryOfBirthDialogFragment()
            countryOfBirthDialogFragment?.show(childFragmentManager, null)
            childFragmentManager.findFragmentById(R.id.countryOfBirthSpinner)?.onStart()
        }
    }

    fun onCountrySelected(country: Country) {
        patientCountry = country
        binding.countryOfBirth.text = country.getLabel(requireContext())
    }

    private fun setupObservers() {
        viewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading()
                    hideSoftKeys()
                }

                is Result.Success -> if (result.operationType == PatientRegistering) {
                    startPatientDashboardActivity(result.data)
                    finishActivity()
                }

                is Result.Error -> if (result.operationType == PatientRegistering) {
                    hideLoading()
                    if (result.throwable.cause is UnknownHostException)
                        ToastUtil.error(getString(R.string.no_internet_connection))
                    else
                        ToastUtil.error(getString(R.string.register_patient_error))
                }

                else -> throw IllegalStateException()
            }
        }

        viewModel.similarPatientsLiveData.observe(viewLifecycleOwner) { similarPatients ->
            hideLoading()
            if (similarPatients.isEmpty()) registerPatient()
            else showSimilarPatientsDialog(similarPatients, viewModel.patient)
        }

        viewModel.isNameValidLiveData.observe(viewLifecycleOwner) { isValid ->
            binding.textInputLayoutFirstName.isErrorEnabled = isValid.first
            binding.textInputLayoutFirstName.error = isValid.second?.let { getString(it) }
        }

        viewModel.isSurnameValidLiveData.observe(viewLifecycleOwner) { isValid ->
            binding.textInputLayoutSurname.isErrorEnabled = isValid.first
            binding.textInputLayoutSurname.error = isValid.second?.let { getString(it) }
        }

        viewModel.isCountryOfBirthValidLiveData.observe(viewLifecycleOwner) { isValid ->
            if (isValid) {
                binding.countryOfBirthLayout.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.corner_transparent_box, null)
            } else {
                binding.countryOfBirthLayout.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.corner_red_box, null)
            }
        }

        viewModel.isGenderValidLiveData.observe(viewLifecycleOwner) { isValid ->
            if (isValid) {
                binding.gendererror.makeGone()
            } else {
                binding.gendererror.makeVisible()
            }
        }

        viewModel.isBirthDateValidLiveData.observe(viewLifecycleOwner) { isValid ->
            binding.textInputLayoutDOB.isErrorEnabled = isValid.first
            binding.textInputLayoutYear.isErrorEnabled = isValid.first
            binding.textInputLayoutDOB.error =
                isValid.second?.let { getString(R.string.empty_value) }
            binding.textInputLayoutYear.error = isValid.second?.let { getString(it) }
        }

        viewModel.isLegalConsentValidLiveData.observe(viewLifecycleOwner) { isValid ->
            if (isValid) {
                binding.languageSpinner.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.admission_spinner, null)
                binding.recordConsentSaved.makeVisible()
                binding.recordLegalConsent.text =
                    context?.getString(R.string.record_again_legal_consent)
                ToastUtil.showShortToast(
                    requireContext(),
                    ToastUtil.ToastType.SUCCESS,
                    R.string.recording_success
                )
            } else {
                binding.languageSpinner.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.admission_spinner_error, null)
            }
        }

        viewModel.isPatientValidLiveData.observe(viewLifecycleOwner) { isValid ->
            if (isValid) {
                binding.submitButton.isEnabled = true
                binding.submitButton.setBackgroundColor(
                    resources.getColor(
                        R.color.color_accent,
                        null
                    )
                )
            } else {
                binding.submitButton.isEnabled = false
                binding.submitButton.setBackgroundColor(
                    resources.getColor(
                        R.color.dark_grey_for_stroke,
                        null
                    )
                )
            }
        }
    }

    private fun findSimilarPatients() {
        viewModel.fetchSimilarPatients()
    }

    fun registerPatient() {
        viewModel.confirmPatient()
    }

    private fun updatePatient() {
        viewModel.patientUpdateLiveData.observeOnce(viewLifecycleOwner) {
            val patientName = viewModel.patient.name.nameString
            when (it) {
                ResultType.PatientUpdateSuccess -> {
                    ToastUtil.success(
                        String.format(
                            getString(R.string.update_patient_success),
                            patientName
                        )
                    )
                    finishActivity()
                }

                ResultType.PatientUpdateLocalSuccess -> {
                    ToastUtil.notify(getString(R.string.no_internet_connection))
                    finishActivity()
                }

                else -> {
                    ToastUtil.error(
                        String.format(
                            getString(R.string.update_patient_error),
                            patientName
                        )
                    )
                    hideLoading()
                }
            }
        }
        viewModel.confirmPatient()
    }

    private fun fillFormFields() {
        if (!viewModel.isUpdatePatient) return

        validateFieldsForUpdatePatient()

        with(viewModel.patient) {
            // Change to Update Patient Form
            requireActivity().title = getString(R.string.action_update_patient_data)

            binding.firstName.setText(name.givenName)
            binding.surname.setText(name.familyName)

            if (notNull(birthdate) || notEmpty(birthdate)) {
                viewModel.dateHolder = convertTimeString(birthdate)
                binding.dobEditText.setText(
                    convertTime(
                        convertTime(
                            viewModel.dateHolder.toString(),
                            DateUtils.OPEN_MRS_REQUEST_FORMAT
                        )!!,
                        DateUtils.DEFAULT_DATE_FORMAT
                    )
                )
            }
            when (gender) {
                "M" -> {
                    binding.gender.check(R.id.male)
                }
                "F" -> {
                    binding.gender.check(R.id.female)
                }
                "N" -> {
                    binding.gender.check(R.id.nonBinary)
                }
            }

            val countryOfBirthLabel = attributes
                .firstOrNull { it.attributeType?.uuid == BuildConfig.COUNTRY_OF_BIRTH_ATTRIBUTE_TYPE_UUID }
                ?.value
                ?.uppercase()

            if (countryOfBirthLabel === null) {
                binding.countryOfBirth.text = context?.getString(R.string.country_of_birth_default)
            } else {
                patientCountry = Country.valueOf(countryOfBirthLabel)
                binding.countryOfBirth.text = patientCountry?.getLabel(requireContext())
            }

            binding.linearLayoutConsent.makeGone()
            addBottomMargin()
        }
    }

    private fun addListenersToAllFields() = with(binding) {

        isNameValid()

        isBirthDateValid()

        isCountryOfBirthValid()
    }

    private fun validateFieldsForUpdatePatient() {
            viewModel.validateFirstName(getInput(binding.firstName))
            viewModel.validateSurname(getInput(binding.surname))
            viewModel.validateCountryOfBirth(binding.countryOfBirth.text.toString())
            viewModel.validateBirthDate(getInput(binding.dobEditText))
            viewModel.validateGender(true)
            viewModel.validateLegalConsent(true)
    }

    private fun isCountryOfBirthValid() = with(binding) {

        countryOfBirth.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.validateCountryOfBirth(countryOfBirth.text.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun isBirthDateValid() = with(binding) {
        dobEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (getInput(dobEditText)?.length == 10) {
                    viewModel.validateBirthDate(getInput(dobEditText))
                } else {
                    binding.textInputLayoutDOB.isErrorEnabled = true
                    binding.textInputLayoutYear.isErrorEnabled = true
                    binding.textInputLayoutDOB.error = getString(R.string.empty_value)
                    binding.textInputLayoutYear.error = getString(R.string.empty_value)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (estimatedYear.text.isNotEmpty()) estimatedYear.text.clear()
            }
        })

        estimatedYear.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.validateBirthDate(getInput(estimatedYear))
            }

            override fun afterTextChanged(s: Editable?) {
                if (dobEditText.text.isNotEmpty()) dobEditText.text.clear()
            }
        })
    }

    private fun isNameValid() = with(binding) {

        firstName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(name: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.validateFirstName(getInput(firstName))
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        surname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.validateSurname(getInput(surname))
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }


    private fun showSimilarPatientsDialog(patients: List<Patient>, patient: Patient) {
        edu.upc.openmrs.bundle.CustomDialogBundle().apply {
            titleViewMessage = getString(R.string.similar_patients_dialog_title)
            rightButtonText = getString(R.string.dialog_button_register_new)
            rightButtonAction =
                CustomFragmentDialog.OnClickAction.REGISTER_PATIENT
            leftButtonText = getString(R.string.dialog_button_cancel)
            leftButtonAction =
                CustomFragmentDialog.OnClickAction.DISMISS
            patientsList = patients
            newPatient = patient
        }.let {
            (requireActivity() as AddEditPatientActivity)
                .createAndShowDialog(it, ApplicationConstants.DialogTAG.SIMILAR_PATIENTS_TAG)
        }
    }


    private fun setupViewsListeners() = with(binding) {

        makeRecordLegalConsentUnderlined()

        setLanguagesOptionsInSpinner()

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Enable the submit button if a valid language (not the first item) is selected
                if (position != 0) {
                    recordLegalConsent.isEnabled = true
                    recordLegalConsent.setTextColor(resources.getColor(R.color.color_accent, null))
                } else {
                    recordLegalConsent.isEnabled = false
                    recordLegalConsent.setTextColor(resources.getColor(R.color.dark_grey_6x, null))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        recordLegalConsent.setOnClickListener {
            val selectedLanguage = languageSpinner.selectedItem.toString()
            showLegalConsent(selectedLanguage)
        }

        submitButton.setOnClickListener {
            submitAction()
        }

        gender.setOnCheckedChangeListener { _, _ ->
            viewModel.validateGender(true)
            gendererror.makeGone()
        }

        DateOfBirthTextWatcher(dobEditText, estimatedYear).let {
            dobEditText.addTextChangedListener(it)
        }

        textInputLayoutDOB.setEndIconOnClickListener {
            val cYear: Int
            val cMonth: Int
            val cDay: Int
            if (viewModel.dateHolder == null) Calendar.getInstance().let {
                cYear = it[Calendar.YEAR]
                cMonth = it[Calendar.MONTH]
                cDay = it[Calendar.DAY_OF_MONTH]
            } else viewModel.dateHolder!!.run {
                cYear = year
                cMonth = monthOfYear - 1
                cDay = dayOfMonth
            }
            estimatedYear.text.clear()

            val dateSetListener =
                { _: DatePicker?, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                    val adjustedMonth = selectedMonth + 1
                    dobEditText.setText(
                        String.format(
                            "%02d",
                            selectedDay
                        ) + "/" + String.format("%02d", adjustedMonth) + "/" + selectedYear
                    )
                    viewModel.dateHolder =
                        LocalDate(selectedYear, adjustedMonth, selectedDay).toDateTimeAtStartOfDay()
                }
            DatePickerDialog(requireActivity(), dateSetListener, cYear, cMonth, cDay).apply {
                datePicker.maxDate = System.currentTimeMillis()
                setTitle(getString(R.string.date_picker_title))
            }.show()
        }
    }

    private fun FragmentPatientInfoBinding.makeRecordLegalConsentUnderlined() {
        recordLegalConsent.paintFlags = recordLegalConsent.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setLanguagesOptionsInSpinner() {
        val spinner = _binding!!.languageSpinner
        val languagesArray = resources.getStringArray(R.array.languages)

        val languagesAdapter =
            ArrayAdapter<Any?>(requireContext(), R.layout.spinner_list, languagesArray)
        languagesAdapter.setDropDownViewResource(R.layout.spinner_list)

        spinner.adapter = languagesAdapter
    }

    private fun showLegalConsent(language: String) {

        if (isMicrophonePresent()) {
            legalConsentDialog = LegalConsentDialogFragment.newInstance(language)
            legalConsentDialog?.show(childFragmentManager, LegalConsentDialogFragment.TAG)
            childFragmentManager.findFragmentById(R.id.linearLayout_consent)?.onStart()
        } else {
            Toast.makeText(
                requireContext(),
                "Microphone Not Detected",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun askPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
            ||
            ActivityCompat.checkSelfPermission(requireContext(), RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(WRITE_EXTERNAL_STORAGE, RECORD_AUDIO), REQUEST_AUDIO_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        // this method is called when user will
        // grant the permission for audio recording.
        when (requestCode) {
            REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.isNotEmpty()) {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord && permissionToStore) {
                    Toast.makeText(
                        requireContext(),
                        "Permission Granted",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Permission Denied",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun isMicrophonePresent(): Boolean {
        return requireActivity().packageManager
            .hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    }

    private fun showLoading() {
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        binding.transpScreenScreen.makeVisible()
        binding.progressBar.makeVisible()
    }

    private fun hideLoading() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.transpScreenScreen.makeGone()
        binding.progressBar.makeGone()
    }

    fun isLoading(): Boolean = viewModel.result.value is Result.Loading

    private fun submitAction() = with(viewModel) {
        var selectedGender =
            view?.findViewById<RadioButton>(binding.gender.checkedRadioButtonId)?.tag.toString()

        with(binding) {
                viewModel.setPatientData(
                    firstName.text.toString(),
                    surname.text.toString(),
                    dobEditText.text.toString(),
                    estimatedYear.text.toString(),
                    selectedGender,
                    patientCountry!!.name
                )
            }

        // New patient registering
        if (!isUpdatePatient) {
            findSimilarPatients()
            return
        }
        updatePatient()
    }

    private fun resetAction() = with(binding) {
        firstName.setText("")
        surname.setText("")
        dobEditText.setText("")
        estimatedYear.setText("")
        gender.clearCheck()
        dobError.text = ""
        gendererror.makeGone()
        countryOfBirthError.makeGone()
        recordConsentError.makeGone()
        textInputLayoutFirstName.error = ""
        textInputLayoutSurname.error = ""
        viewModel.resetPatient()
    }

    private fun hideSoftKeys() {
        requireActivity().let {
            val view = it.currentFocus ?: View(it)
            val inputMethodManager =
                it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun isAnyFieldNotEmpty(): Boolean = with(binding) {
        return !isEmpty(firstName) || !isEmpty(surname) ||
                !isEmpty(dobEditText) || !isEmpty(estimatedYear) ||
                isLegalConsent() || isNationality()
    }

    private fun isNationality() =
        binding.countryOfBirth.text != context?.getString(R.string.country_of_birth_default)

    private fun isLegalConsent() = viewModel.isLegalConsentValidLiveData.value == true

    private fun startPatientDashboardActivity(patient: Patient) {
        Intent(requireActivity(), PatientDashboardActivity::class.java).apply {
            putExtra(PATIENT_ID_BUNDLE, patient.id)
            putExtra(PATIENT_UUID_BUNDLE, patient.uuid)
            startActivity(this)
        }
    }

    private fun finishActivity() = requireActivity().finish()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.submit_done_menu, menu)
        if (viewModel.isUpdatePatient) {
            // Remove reset button when updating a patient
            menu.findItem(R.id.actionReset).run {
                isVisible = false
                isEnabled = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> requireActivity().onBackPressed()
            R.id.actionReset -> AlertDialog.Builder(requireActivity())
                .setTitle(R.string.dialog_title_reset_patient)
                .setMessage(R.string.reset_dialog_message)
                .setPositiveButton(R.string.dialog_button_ok) { dialogInterface: DialogInterface?, i: Int -> resetAction() }
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .show()

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(patientID: String?) =
            AddEditPatientFragment().apply {
                arguments = bundleOf(Pair(PATIENT_ID_BUNDLE, patientID))
            }

        private const val REQUEST_AUDIO_PERMISSION_CODE = 200
    }
}
