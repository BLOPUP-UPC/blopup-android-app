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

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.LinearLayout.LayoutParams
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.annotation.StringDef
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import com.google.android.libraries.places.api.Places
import com.google.android.material.snackbar.Snackbar
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCrop.REQUEST_CROP
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.BuildConfig
import edu.upc.R
import edu.upc.blopup.toggles.check
import edu.upc.blopup.toggles.showPatientConsentToggle
import edu.upc.databinding.FragmentPatientInfoBinding
import edu.upc.openmrs.activities.BaseFragment
import edu.upc.openmrs.activities.dialog.CustomPickerDialog.onInputSelected
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardActivity
import edu.upc.openmrs.listeners.watcher.PatientBirthdateValidatorWatcher
import edu.upc.openmrs.utilities.*
import edu.upc.openmrs.utilities.ViewUtils.getInput
import edu.upc.openmrs.utilities.ViewUtils.isEmpty
import edu.upc.sdk.library.models.*
import edu.upc.sdk.library.models.OperationType.PatientRegistering
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.COUNTRIES_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.URI_IMAGE
import edu.upc.sdk.utilities.DateUtils
import edu.upc.sdk.utilities.DateUtils.convertTime
import edu.upc.sdk.utilities.DateUtils.convertTimeString
import edu.upc.sdk.utilities.DateUtils.getDateTimeFromDifference
import edu.upc.sdk.utilities.DateUtils.validateDate
import edu.upc.sdk.utilities.StringUtils.ILLEGAL_CHARACTERS
import edu.upc.sdk.utilities.StringUtils.isBlank
import edu.upc.sdk.utilities.StringUtils.notEmpty
import edu.upc.sdk.utilities.StringUtils.notNull
import edu.upc.sdk.utilities.StringUtils.validateText
import edu.upc.sdk.utilities.ToastUtil
import kotlinx.android.synthetic.main.fragment_dialog_layout.*
import kotlinx.android.synthetic.main.fragment_matching_patients.view.*
import kotlinx.android.synthetic.main.fragment_patient_info.*
import kotlinx.android.synthetic.main.legal_consent.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.ktx.PermissionsRequester
import permissions.dispatcher.ktx.constructPermissionsRequest
import java.io.File
import java.util.*


@AndroidEntryPoint
class AddEditPatientFragment : BaseFragment(), onInputSelected {
    var alertDialog: AlertDialog? = null
    private var legalConsentDialog: LegalConsentDialogFragment? = null
    private var _binding: FragmentPatientInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditPatientViewModel by viewModels()

    // constant for storing audio permission
    private val REQUEST_AUDIO_PERMISSION_CODE = 200

    private lateinit var cameraAndStoragePermissions: PermissionsRequester
    private lateinit var storageWritePermission: PermissionsRequester

    private val pickPhoto = registerForActivityResult(GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        val destinationUri = Uri.fromFile(
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                ImageUtils.createUniqueImageFileName()
            )
        )
        startCropActivity(uri, destinationUri)
    }

    private val capturePhoto = registerForActivityResult(TakePicture()) { resultOk ->
        if (!resultOk) return@registerForActivityResult
        val sourceUri = Uri.fromFile(viewModel.capturedPhotoFile)
        startCropActivity(sourceUri, sourceUri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPatientInfoBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        setupPermissionsHandler()

        setupObservers()

        initPlaces()

        setupViewsListeners()

        fillFormFields()

        askPermissions()

        showPatientConsentToggle.check(onToggleDisabled = {
            binding.linearLayoutConsent.makeGone()

            val densityOperator = context?.resources?.displayMetrics?.density?.toInt()

            val bottomMargin = binding.linearLayoutNationality.layoutParams as LayoutParams
            bottomMargin.bottomMargin = densityOperator!! * 90
            binding.linearLayoutNationality.layoutParams = bottomMargin
        })
        return binding.root
    }

    private fun setupPermissionsHandler() {
        cameraAndStoragePermissions = constructPermissionsRequest(
            Manifest.permission.CAMERA, WRITE_EXTERNAL_STORAGE,
            onShowRationale = ::showCameraPermissionRationale,
            onPermissionDenied = { showSnackbarLong(R.string.permissions_camera_storage_denied) },
            onNeverAskAgain = { showSnackbarLong(R.string.permissions_camera_storage_neverask) },
            requiresPermission = ::capturePhoto
        )
        storageWritePermission = constructPermissionsRequest(
            WRITE_EXTERNAL_STORAGE,
            onShowRationale = { request -> request.proceed() },
            onNeverAskAgain = { showSnackbarLong(R.string.permission_storage_neverask) },
            requiresPermission = ::pickPhoto
        )
    }

    private fun setupObservers() {
        viewModel.result.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading()
                    hideSoftKeys()
                }
                is Result.Success -> if (result.operationType == PatientRegistering) {
                    startPatientDashboardActivity()
                    finishActivity()
                }
                is Result.Error -> if (result.operationType == PatientRegistering) {
                    hideLoading()
                    ToastUtil.error(getString(R.string.register_patient_error))
                }
                else -> throw IllegalStateException()
            }
        })
        viewModel.similarPatientsLiveData.observe(viewLifecycleOwner, Observer { similarPatients ->
            hideLoading()
            if (similarPatients.isEmpty()) registerPatient()
            else showSimilarPatientsDialog(similarPatients, viewModel.patient)
        })
    }

    private fun findSimilarPatients() {
        validateFormInputsAndUpdateViewModel()
        viewModel.fetchSimilarPatients()
    }

    fun registerPatient() {
        validateFormInputsAndUpdateViewModel()
        viewModel.confirmPatient()
    }

    private fun updatePatient() {
        validateFormInputsAndUpdateViewModel()
        viewModel.patientUpdateLiveData.observeOnce(viewLifecycleOwner, Observer {
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
                    ToastUtil.notify(getString(R.string.offline_mode_patient_data_saved_locally_notification_message))
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
        })
        viewModel.confirmPatient()
    }

    private fun validateRecordConsent(): Boolean {
        //#region -- If Record Consent Is Missing --
        if (!FileUtils.fileIsCreatedSuccessfully(legalConsentDialog?.fileName())) {
            binding.recordConsentError.makeVisible()
            return false
        } else
            binding.recordConsentError.makeGone()
        //#endreigon
        return true
    }

    private fun fillFormFields() {
        if (!viewModel.isUpdatePatient) return
        with(viewModel.patient) {
            // Change to Update Patient Form
            requireActivity().title = getString(R.string.action_update_patient_data)

            // No need for un-identification option once the patient is registered
            binding.unidentifiedCheckbox.makeGone()
            // Show deceased option only when patient is registered
            binding.deceasedCardview.makeVisible()

            binding.firstName.setText(name.givenName)
            binding.middlename.setText(name.middleName)
            binding.surname.setText(name.familyName)
            binding.phoneNumber.setText(phoneNumber)
            binding.documentId.setText(documentId)

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
            if (StringValue.MALE == gender) {
                binding.gender.check(R.id.male)
            } else if (StringValue.FEMALE == gender) {
                binding.gender.check(R.id.female)
            } else if (StringValue.NON_BINARY == gender) {
                binding.gender.check(R.id.nonBinary)
            }
            if (photo != null) binding.patientPhoto.setImageBitmap(resizedPhoto)

            binding.deceasedCheckbox.isChecked = isDeceased
        }
    }

    private fun validateFormInputsAndUpdateViewModel() = with(binding) {
        viewModel.isPatientUnidentified = unidentifiedCheckbox.isChecked
        viewModel.patient.isDeceased = deceasedCheckbox.isChecked

        if (unidentifiedCheckbox.isChecked) {
            /* Names */
            viewModel.patient.names = listOf(PersonName().apply {
                familyName = getString(R.string.unidentified_patient_name)
                givenName = getString(R.string.unidentified_patient_name)
            })

            /* Birth date */
            if (isBlank(getInput(estimatedYear)) && isBlank(getInput(estimatedMonth))) {
                dobError.text = getString(R.string.dob_error_for_unidentified)
                dobError.makeVisible()
                scrollToTop()
            } else {
                dobError.makeGone()
                viewModel.patient.birthdateEstimated = true
                val yearDiff =
                    if (isEmpty(estimatedYear)) 0 else estimatedYear.text.toString().toInt()
                val monthDiff =
                    if (isEmpty(estimatedMonth)) 0 else estimatedMonth.text.toString().toInt()
                viewModel.dateHolder = getDateTimeFromDifference(yearDiff, monthDiff)
                viewModel.patient.birthdate =
                    DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT)
                        .print(viewModel.dateHolder)
            }

            /* Gender */
            val genderChoices =
                arrayOf(StringValue.MALE, StringValue.FEMALE, StringValue.NON_BINARY)
            val index =
                gender.indexOfChild(requireActivity().findViewById(gender.checkedRadioButtonId))
            if (index == -1) {
                gendererror.makeVisible()
                viewModel.patient.gender = null
                scrollToTop()
            } else {
                gendererror.makeGone()
                viewModel.patient.gender = genderChoices[index]
            }
            Log.i("payload", viewModel.patient.toString())
            return

        }

        /* Names */

        // First name validation
        if (isEmpty(firstName)) {
            textInputLayoutFirstName.isErrorEnabled = true
            textInputLayoutFirstName.error = getString(R.string.emptyerror)
            scrollToTop()
        } else if (!validateText(getInput(firstName), ILLEGAL_CHARACTERS)) {
            textInputLayoutFirstName.isErrorEnabled = true
            textInputLayoutFirstName.error = getString(R.string.fname_invalid_error)
            scrollToTop()
        } else {
            textInputLayoutFirstName.isErrorEnabled = false
        }

        // Middle name validation (can be empty)
        if (!validateText(getInput(middlename), ILLEGAL_CHARACTERS)) {
            textInputLayoutMiddlename.isErrorEnabled = true
            textInputLayoutMiddlename.error = getString(R.string.midname_invalid_error)
            scrollToTop()
        } else {
            textInputLayoutMiddlename.isErrorEnabled = false
        }

        // Family name validation
        if (isEmpty(surname)) {
            textInputLayoutSurname.isErrorEnabled = true
            textInputLayoutSurname.error = getString(R.string.emptyerror)
            scrollToTop()
        } else if (!validateText(getInput(surname), ILLEGAL_CHARACTERS)) {
            textInputLayoutSurname.isErrorEnabled = true
            textInputLayoutSurname.error = getString(R.string.lname_invalid_error)
            scrollToTop()
        } else {
            textInputLayoutSurname.isErrorEnabled = false
        }

        viewModel.patient.names = listOf(PersonName().apply {
            givenName = getInput(firstName)
            middleName = getInput(middlename)
            familyName = getInput(surname)
        })

        /* Gender */
        val genderChoices = arrayOf(StringValue.MALE, StringValue.FEMALE, StringValue.NON_BINARY)
        val index = gender.indexOfChild(requireActivity().findViewById(gender.checkedRadioButtonId))
        if (index == -1) {
            gendererror.makeVisible()
            scrollToTop()
            viewModel.patient.gender = null
        } else {
            gendererror.makeGone()
            viewModel.patient.gender = genderChoices[index]
        }

        /* Birth date */
        if (isEmpty(dobEditText)) {
            if (isBlank(getInput(estimatedYear)) && isBlank(getInput(estimatedMonth))) {
                val dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT)
                val minimumDate = DateTime.now().minusYears(
                    ApplicationConstants.RegisterPatientRequirements.MAX_PATIENT_AGE
                )
                    .toString(dateTimeFormatter)
                val maximumDate = DateTime.now().toString(dateTimeFormatter)
                dobError.text = getString(R.string.dob_error, minimumDate, maximumDate)
                dobError.makeVisible()
                scrollToTop()
            } else {
                viewModel.patient.birthdateEstimated = true
                val yearDiff =
                    if (isEmpty(estimatedYear)) 0 else estimatedYear.text.toString().toInt()
                val monthDiff =
                    if (isEmpty(estimatedMonth)) 0 else estimatedMonth.text.toString().toInt()
                viewModel.dateHolder = getDateTimeFromDifference(yearDiff, monthDiff)
                viewModel.patient.birthdate =
                    DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT)
                        .print(viewModel.dateHolder)
                dobError.makeGone()
            }
        } else {
            viewModel.patient.birthdateEstimated = false
            val insertedDate = dobEditText.text.toString().trim { it <= ' ' }
            val minDateOfBirth = DateTime.now().minusYears(
                ApplicationConstants.RegisterPatientRequirements.MAX_PATIENT_AGE
            )
            val maxDateOfBirth = DateTime.now()
            if (validateDate(insertedDate, minDateOfBirth, maxDateOfBirth)) {
                val dateTimeFormatter = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT)
                viewModel.dateHolder = dateTimeFormatter.parseDateTime(insertedDate)
            }
            dobError.makeGone()
            viewModel.patient.birthdate =
                DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT)
                    .print(viewModel.dateHolder)
        }

        /* Nationality */
        if (null == countryCodeSpinner.cpViewHelper.selectedCountry.value) {
            nationalityerror.makeVisible()
            scrollToTop()
        } else {
            nationalityerror.makeGone()
            viewModel.patient.attributes = listOf(PersonAttribute().apply {
                attributeType = PersonAttributeType().apply {
                    uuid = BuildConfig.NATIONALITY_ATTRIBUTE_TYPE_UUID
                    value = countryCodeSpinner.cpViewHelper.selectedCountry.value!!.name
                }
            })
        }

        /* Legal Consent */
        val validateLegalConsent = validateRecordConsent()
        if (validateLegalConsent) {
            if (viewModel.patient.attributes.any()) {
                viewModel.patient.attributes =
                    viewModel.patient.attributes.plus(PersonAttribute().apply {
                        attributeType = PersonAttributeType().apply {
                            uuid = BuildConfig.LEGAL_CONSENT_ATTRIBUTE_TYPE_UUID
                            value = legalConsentDialog?.fileName()
                        }
                    })
            } else {
                viewModel.patient.attributes = listOf(PersonAttribute().apply {
                    attributeType = PersonAttributeType().apply {
                        uuid = BuildConfig.LEGAL_CONSENT_ATTRIBUTE_TYPE_UUID
                        value = legalConsentDialog?.fileName()
                    }
                })
            }
        }
    }

    private fun showSimilarPatientsDialog(patients: List<Patient>, patient: Patient) {
        edu.upc.openmrs.bundle.CustomDialogBundle().apply {
            titleViewMessage = getString(R.string.similar_patients_dialog_title)
            rightButtonText = getString(R.string.dialog_button_register_new)
            rightButtonAction =
                edu.upc.openmrs.activities.dialog.CustomFragmentDialog.OnClickAction.REGISTER_PATIENT
            leftButtonText = getString(R.string.dialog_button_cancel)
            leftButtonAction =
                edu.upc.openmrs.activities.dialog.CustomFragmentDialog.OnClickAction.DISMISS
            patientsList = patients
            newPatient = patient
        }.let {
            (requireActivity() as AddEditPatientActivity)
                .createAndShowDialog(it, ApplicationConstants.DialogTAG.SIMILAR_PATIENTS_TAG)
        }
    }

    private fun setupViewsListeners() = with(binding) {
        unidentifiedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                linearLayoutName.makeGone()
                constraintLayoutDOB.makeGone()
                viewModel.isPatientUnidentified = true
            } else {
                linearLayoutName.makeVisible()
                constraintLayoutDOB.makeVisible()
                viewModel.isPatientUnidentified = false
            }

        }

        recordConsentImageButton.setOnClickListener {
            showLegalConsent()
        }

        submitButton.setOnClickListener {
            submitAction()
        }

        gender.setOnCheckedChangeListener { _, _ -> gendererror.makeGone() }

        dobEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // No need for this method
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Auto-add slash before entering month (e.g. "17/*") and before entering year (e.g. "17/10/*")
                dobEditText.text.toString().let {
                    if ((it.length == 3 && !it.contains("/")) ||
                        (it.length == 6 && !it.substring(3).contains("/"))
                    ) {
                        dobEditText.setText(StringBuilder(it).insert(it.length - 1, "/").toString())
                        dobEditText.setSelection(dobEditText.text.length)
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                // If a considerable amount of text is filled in dobEditText, then remove 'Estimated age' fields.
                if (s.length >= 10) {
                    estimatedMonth.text.clear()
                    estimatedYear.text.clear()
                }
            }
        })

        datePicker.setOnClickListener {
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
            estimatedMonth.text.clear()
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

        PatientBirthdateValidatorWatcher(dobEditText, estimatedMonth, estimatedYear).let {
            estimatedMonth.addTextChangedListener(it)
            estimatedYear.addTextChangedListener(it)
        }

        capturePhoto.setOnClickListener {
            val dialogList = mutableListOf(
                edu.upc.openmrs.activities.dialog.CustomDialogModel(
                    getString(R.string.dialog_take_photo),
                    R.drawable.ic_photo_camera
                ),
                edu.upc.openmrs.activities.dialog.CustomDialogModel(
                    getString(R.string.dialog_choose_photo),
                    R.drawable.ic_photo_library
                )
            )
            if (viewModel.patient.photo != null) {
                dialogList.add(
                    edu.upc.openmrs.activities.dialog.CustomDialogModel(
                        getString(R.string.dialog_remove_photo),
                        R.drawable.ic_photo_delete
                    )
                )
            }
            edu.upc.openmrs.activities.dialog.CustomPickerDialog(dialogList)
                .apply { setTargetFragment(this@AddEditPatientFragment, 1000) }
                .show(requireFragmentManager(), "tag")
        }

        patientPhoto.setOnClickListener {
            if (viewModel.capturedPhotoFile != null) {
                val i = Intent(Intent.ACTION_VIEW)
                i.setDataAndType(
                    Uri.fromFile(viewModel.capturedPhotoFile),
                    ApplicationConstants.IMAGE_JPEG
                )
                startActivity(i)
            } else if (viewModel.patient.photo != null) {
                viewModel.patient.run {
                    ImageUtils.showPatientPhoto(
                        requireContext(),
                        photo,
                        name.nameString
                    )
                }
            }
        }

        deceasedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                deceasedProgressBar.makeVisible()
                deceasedSpinner.makeGone()
                showCauseOfDeathOptions()
            } else {
                deceasedProgressBar.makeGone()
                deceasedSpinner.makeGone()
                viewModel.patient.isDeceased = false
                viewModel.patient.causeOfDeath = null
            }
        }
    }

    private fun showCauseOfDeathOptions() {
        viewModel.fetchCausesOfDeath().observeOnce(viewLifecycleOwner, Observer {
            if (it.answers.isNotEmpty()) updateCauseOfDeathSpinner(it)
            else showCannotMarkDeceased()
        })
    }

    private fun showCannotMarkDeceased() = with(binding) {
        deceasedProgressBar.makeGone()
        deceasedSpinner.makeGone()
        deceasedCheckbox.isChecked = false
        ToastUtil.error(getString(R.string.mark_patient_deceased_no_concepts))
    }

    private fun updateCauseOfDeathSpinner(concept: ConceptAnswers) = with(binding) {
        deceasedProgressBar.makeGone()
        deceasedSpinner.makeVisible()

        val answers = concept.answers
        val answerDisplays = arrayOfNulls<String>(answers.size)
        for (i in answers.indices) {
            answerDisplays[i] = answers[i].display
        }

        deceasedSpinner.adapter =
            ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, answerDisplays)
        deceasedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                pos: Int,
                l: Long,
            ) {
                val display = deceasedSpinner.selectedItem.toString()
                for (i in answers.indices) {
                    if (display == answers[i].display) {
                        viewModel.patient.causeOfDeath = answers[i]
                    }
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun initPlaces() {
        if (viewModel.placesClient != null) return
        with(requireActivity()) {
            val applicationInfo =
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val placesApiKey = applicationInfo.metaData.getString("com.google.android.geo.API_KEY")
            if (!Places.isInitialized() && placesApiKey != null) {
                Places.initialize(applicationContext, placesApiKey)
                viewModel.placesClient = Places.createClient(this)
            }
        }
    }

    override fun performFunction(position: Int) = when (position) {
        0 -> {
            // Capture photo
            StrictMode.VmPolicy.Builder().run { StrictMode.setVmPolicy(build()) }
            cameraAndStoragePermissions.launch()
        }
        1 -> {
            // Pick photo from gallery
            storageWritePermission.launch()
        }
        2 -> {
            // Remove photo
            binding.patientPhoto.setImageResource(R.drawable.ic_person_grey_500_48dp)
            binding.patientPhoto.invalidate()
            viewModel.patient.photo = null
        }
        else -> {
        }
    }

    private fun capturePhoto() = with(viewModel) {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        capturedPhotoFile = File(dir, ImageUtils.createUniqueImageFileName())
        capturePhoto.launch(Uri.fromFile(capturedPhotoFile))
    }

    private fun pickPhoto() = pickPhoto.launch(URI_IMAGE)

    private fun showCameraPermissionRationale(request: PermissionRequest) {
        AlertDialog.Builder(requireActivity())
            .setMessage(R.string.permissions_camera_storage_rationale)
            .setPositiveButton(R.string.button_allow) { _: DialogInterface?, _: Int -> request.proceed() }
            .setNegativeButton(R.string.button_deny) { _: DialogInterface?, _: Int -> request.cancel() }
            .show()
    }

    private fun showLegalConsent() {
        if (isMicrophonePresent()) {
            legalConsentDialog = LegalConsentDialogFragment()
            legalConsentDialog?.show(childFragmentManager, LegalConsentDialogFragment.TAG)
            childFragmentManager.findFragmentById(R.id.legal_consent)?.onStart()
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

    private fun showSnackbarLong(stringId: Int) {
        Snackbar.make(binding.addEditConstraintLayout, stringId, Snackbar.LENGTH_LONG)
            .apply {
                view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    .setTextColor(Color.WHITE)
            }
            .show()
    }

    private fun submitAction() = with(viewModel) {
        // New patient registering
        if (!isUpdatePatient) {
            findSimilarPatients()
            return@with
        }
        // Existing patient updating
        if (patient.isDeceased && !patient.causeOfDeath.uuid.isNullOrEmpty()) {
            alertDialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(R.string.mark_patient_deceased)
                .setMessage(R.string.mark_patient_deceased_notice)
                .setCancelable(false)
                .setPositiveButton(R.string.mark_patient_deceased_proceed) { _, _ ->
                    alertDialog?.cancel()
                    updatePatient()
                }
                .setNegativeButton(R.string.dialog_button_cancel) { _, _ ->
                    alertDialog?.cancel()
                }
                .create()
            alertDialog?.show()
        } else {
            updatePatient()
        }
    }

    private fun resetAction() = with(binding) {
        firstName.setText("")
        middlename.setText("")
        surname.setText("")
        dobEditText.setText("")
        estimatedYear.setText("")
        estimatedMonth.setText("")
        countryCodeSpinner.cpViewHelper.clearSelection()
        gender.clearCheck()
        dobError.text = ""
        gendererror.makeGone()
        nationalityerror.makeGone()
        recordConsentError.makeGone()
        textInputLayoutFirstName.error = ""
        textInputLayoutMiddlename.error = ""
        textInputLayoutSurname.error = ""
        patientPhoto.setImageResource(R.drawable.ic_person_grey_500_48dp)
        viewModel.resetPatient()
    }

    private fun scrollToTop() = binding.run { scrollView.smoothScrollTo(0, scrollView.paddingTop) }

    private fun hideSoftKeys() {
        requireActivity().let {
            val view = it.currentFocus ?: View(it)
            val inputMethodManager =
                it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun isAnyFieldNotEmpty(): Boolean = with(binding) {
        return !isEmpty(firstName) || !isEmpty(middlename) || !isEmpty(surname) ||
                !isEmpty(dobEditText) || !isEmpty(estimatedYear) || !isEmpty(estimatedMonth)
    }


    private fun startCropActivity(sourceUri: Uri, destinationUri: Uri) {
        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(
                ApplicationConstants.ASPECT_RATIO_FOR_CROPPING,
                ApplicationConstants.ASPECT_RATIO_FOR_CROPPING
            )
            .start(requireActivity(), this@AddEditPatientFragment)
    }

    private fun startPatientDashboardActivity() {
        Intent(requireActivity(), PatientDashboardActivity::class.java).apply {
            putExtra(PATIENT_ID_BUNDLE, viewModel.patient.id)
            startActivity(this)
        }
    }

    private fun finishActivity() = requireActivity().finish()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                data?.let { UCrop.getOutput(it) }?.path?.let {
                    viewModel.patient.photo = ImageUtils.getResizedPortraitImage(it)
                    with(binding.patientPhoto) {
                        val bitmap =
                            ThumbnailUtils.extractThumbnail(viewModel.patient.photo, width, height)
                        setImageBitmap(bitmap)
                        invalidate()
                    }
                }
            } else {
                viewModel.capturedPhotoFile = null
                data?.let { ToastUtil.error(UCrop.getError(it)?.message!!) }
            }
        }
    }

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

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(StringValue.MALE, StringValue.FEMALE)
    annotation class StringValue {
        companion object {
            const val FEMALE = "F"
            const val MALE = "M"
            const val NON_BINARY = "N"
        }
    }

    companion object {
        fun newInstance(patientID: String?, countries: List<String>) =
            AddEditPatientFragment().apply {
                arguments = bundleOf(
                    Pair(PATIENT_ID_BUNDLE, patientID),
                    Pair(COUNTRIES_BUNDLE, countries)
                )
            }
    }
}
