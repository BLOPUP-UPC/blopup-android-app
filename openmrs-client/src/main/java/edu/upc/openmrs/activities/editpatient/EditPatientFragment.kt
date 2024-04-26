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
package edu.upc.openmrs.activities.editpatient

import android.app.DatePickerDialog
import android.content.DialogInterface
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
import android.widget.DatePicker
import android.widget.LinearLayout.LayoutParams
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.databinding.FragmentPatientInfoBinding
import edu.upc.openmrs.activities.BaseFragment
import edu.upc.openmrs.activities.editpatient.countryofbirth.Country
import edu.upc.openmrs.activities.editpatient.countryofbirth.CountryOfBirthDialogFragment
import edu.upc.openmrs.listeners.watcher.DateOfBirthTextWatcher
import edu.upc.openmrs.utilities.ViewUtils.getInput
import edu.upc.openmrs.utilities.ViewUtils.isEmpty
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeVisible
import edu.upc.openmrs.utilities.observeOnce
import edu.upc.sdk.library.models.PersonAttribute.Companion.NATIONALITY_ATTRIBUTE_UUID
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.DateUtils.parseLocalDateFromOpenmrsDate
import edu.upc.sdk.utilities.StringUtils.notEmpty
import edu.upc.sdk.utilities.StringUtils.notNull
import edu.upc.sdk.utilities.ToastUtil
import java.util.Calendar


@AndroidEntryPoint
class EditPatientFragment : BaseFragment() {
    private var countryOfBirthDialogFragment: CountryOfBirthDialogFragment? = null
    private var patientCountry: Country? = null
    private var _binding: FragmentPatientInfoBinding? = null
    private val binding get() = _binding!!

    val viewModel: EditPatientViewModel by viewModels()

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

    fun editPatient() {
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
        validateFieldsForUpdatePatient()

        with(viewModel.patient) {
            // Change to Update Patient Form
            requireActivity().title = getString(R.string.action_update_patient_data)

            binding.firstName.setText(name.givenName)
            binding.surname.setText(name.familyName)

            if (notNull(birthdate) || notEmpty(birthdate)) {
                viewModel.dateHolder = parseLocalDateFromOpenmrsDate(birthdate)
                binding.dobEditText.setText(viewModel.dateHolder.toString())
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
                .firstOrNull { it.attributeType?.uuid == NATIONALITY_ATTRIBUTE_UUID }
                ?.value
                ?.uppercase()

            if (countryOfBirthLabel === null) {
                binding.countryOfBirth.text = context?.getString(R.string.country_of_birth_default)
            } else {
                patientCountry = Country.valueOf(countryOfBirthLabel)
                binding.countryOfBirth.text = patientCountry?.getLabel(requireContext())
            }

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

    private fun setupViewsListeners() = with(binding) {

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
                cMonth = monthValue - 1
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
                        java.time.LocalDate.of(selectedYear, adjustedMonth, selectedDay)
                }
            DatePickerDialog(requireActivity(), dateSetListener, cYear, cMonth, cDay).apply {
                datePicker.maxDate = System.currentTimeMillis()
                setTitle(getString(R.string.date_picker_title))
            }.show()
        }
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
        textInputLayoutFirstName.error = ""
        textInputLayoutSurname.error = ""
        viewModel.resetPatient()
    }

    fun isAnyFieldNotEmpty(): Boolean = with(binding) {
        return !isEmpty(firstName) || !isEmpty(surname) ||
                !isEmpty(dobEditText) || !isEmpty(estimatedYear) || isNationality()
    }

    private fun isNationality() =
        binding.countryOfBirth.text != context?.getString(R.string.country_of_birth_default)

    private fun finishActivity() = requireActivity().finish()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.submit_done_menu, menu)
            menu.findItem(R.id.actionReset).run {
                isVisible = false
                isEnabled = false
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> requireActivity().onBackPressed()
            R.id.actionReset -> AlertDialog.Builder(requireActivity())
                .setTitle(R.string.dialog_title_reset_patient)
                .setMessage(R.string.reset_dialog_message)
                .setPositiveButton(R.string.dialog_button_ok) { dialogInterface: DialogInterface?, _: Int -> resetAction() }
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
            EditPatientFragment().apply {
                arguments = bundleOf(Pair(PATIENT_ID_BUNDLE, patientID))
            }
    }
}
