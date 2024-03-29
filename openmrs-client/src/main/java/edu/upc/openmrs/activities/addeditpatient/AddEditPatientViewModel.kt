package edu.upc.openmrs.activities.addeditpatient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.BuildConfig
import edu.upc.R
import edu.upc.blopup.RecordingHelper
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.LegalConsent
import edu.upc.sdk.library.models.OperationType.PatientRegistering
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.PersonAttribute
import edu.upc.sdk.library.models.PersonAttributeType
import edu.upc.sdk.library.models.PersonName
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.RegisterPatientRequirements.MAX_PATIENT_AGE
import edu.upc.sdk.utilities.DateUtils
import edu.upc.sdk.utilities.StringUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class AddEditPatientViewModel @Inject constructor(
    patientDAO: PatientDAO,
    private val patientRepository: PatientRepository,
    private val recordingHelper: RecordingHelper,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<Patient>() {

    private val _similarPatientsLiveData = MutableLiveData<List<Patient>>()
    val similarPatientsLiveData: LiveData<List<Patient>> get() = _similarPatientsLiveData

    private val _patientUpdateLiveData = MutableLiveData<ResultType>()
    val patientUpdateLiveData: LiveData<ResultType> get() = _patientUpdateLiveData

    private val _isNameValidLiveData =
        MutableLiveData<Pair<Boolean, Int?>>(Pair(false, R.string.empty_value))
    val isNameValidLiveData: LiveData<Pair<Boolean, Int?>> get() = _isNameValidLiveData

    private val _isSurnameValidLiveData =
        MutableLiveData<Pair<Boolean, Int?>>(Pair(false, R.string.empty_value))
    val isSurnameValidLiveData: LiveData<Pair<Boolean, Int?>> get() = _isSurnameValidLiveData

    private val _isCountryOfBirthValidLiveData = MutableLiveData(false)
    val isCountryOfBirthValidLiveData: LiveData<Boolean> get() = _isCountryOfBirthValidLiveData

    private val _isGenderValidLiveData = MutableLiveData(false)
    val isGenderValidLiveData: LiveData<Boolean> get() = _isGenderValidLiveData

    private val _isBirthDateValidLiveData = MutableLiveData<Pair<Boolean, Int?>>(Pair(false, R.string.empty_value))
    val isBirthDateValidLiveData: LiveData<Pair<Boolean, Int?>> get() = _isBirthDateValidLiveData

    private val _isLegalConsentValidLiveData = MutableLiveData(false)
    val isLegalConsentValidLiveData: LiveData<Boolean> get() = _isLegalConsentValidLiveData

    private val _isPatientValidLiveData = MutableLiveData(false)
    val isPatientValidLiveData: LiveData<Boolean> get() = _isPatientValidLiveData

    var isUpdatePatient = false
        private set
    lateinit var patient: Patient

    var dateHolder: DateTime? = null
    var legalConsentFileName: String? = null

    init {
        // Initialize patient state
        val patientId: String? = savedStateHandle[PATIENT_ID_BUNDLE]
        val foundPatient = patientDAO.findPatientByID(patientId)
        if (foundPatient != null) {
            isUpdatePatient = true
            patient = foundPatient
        } else {
            resetPatient()
        }
    }

    fun resetPatient() {
        isUpdatePatient = false
        dateHolder = null
        patient = Patient()
    }

    fun confirmPatient() {
        if (isUpdatePatient) updatePatient()
        else {
            registerPatient()
        }

    }

    fun setPatientData(
        firstName: String,
        lastName: String,
        dobEditText: String,
        estimatedYear: String,
        gender: String,
        country: String
    ) {
        with(patient) {
            isDeceased = false
            names = listOf(PersonName().apply {
                givenName = firstName
                familyName = lastName
            })

            if (estimatedYear.isNotEmpty()) {
                birthdateEstimated = true
                val approximateBirthdate =
                    DateUtils.getDateTimeFromDifference(estimatedYear.toInt())
                birthdate = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT)
                    .print(approximateBirthdate)
            } else {
                birthdateEstimated = false
                val parsedBirthdate = DateTime.parse(dobEditText, DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT))
                birthdate = DateTimeFormat.forPattern(DateUtils.OPEN_MRS_REQUEST_PATIENT_FORMAT)
                    .print(parsedBirthdate)
            }

            this.gender = gender

            attributes = listOf(
                PersonAttribute().apply
                {
                    attributeType = PersonAttributeType().apply {
                        uuid = BuildConfig.COUNTRY_OF_BIRTH_ATTRIBUTE_TYPE_UUID
                        value = country
                    }
                })
        }
    }


    fun fetchSimilarPatients() {
        setLoading()
        addSubscription(patientRepository.fetchSimilarPatients(patient)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _similarPatientsLiveData.value = it }
        )
    }

    private fun registerPatient() {
        setLoading()
        addSubscription(
            patientRepository.registerPatient(patient)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        setContent(it, PatientRegistering)
                        recordingHelper.saveLegalConsent(LegalConsent().apply {
                            val patientIdentifier = patient.identifier.identifier
                            if (patientIdentifier != null) {
                                this.patientIdentifier = patientIdentifier
                                this.filePath = legalConsentFileName
                            }
                        })
                    },
                    { setError(it, PatientRegistering) },
                )
        )
    }

    private fun updatePatient() {
        setLoading()
        addSubscription(patientRepository.updatePatient(patient)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resultType ->
                    _patientUpdateLiveData.value = resultType
                },
                { _patientUpdateLiveData.value = ResultType.PatientUpdateError }
            )
        )
    }

    fun validateFirstName(input: String?) {
        if (input.isNullOrBlank()) {
            _isNameValidLiveData.value = Pair(false, R.string.empty_value)
        } else if (!StringUtils.validateText(input, StringUtils.ILLEGAL_CHARACTERS)) {
            _isNameValidLiveData.value = Pair(false, R.string.fname_invalid_error)
        } else {
            _isNameValidLiveData.value = Pair(true, null)
        }
        isEverythingValid()
    }

    fun validateSurname(input: String?) {
        if (input.isNullOrBlank()) {
            _isSurnameValidLiveData.value = Pair(false, R.string.empty_value)
        } else if (!StringUtils.validateText(input, StringUtils.ILLEGAL_CHARACTERS)) {
            _isSurnameValidLiveData.value = Pair(false, R.string.fname_invalid_error)
        } else {
            _isSurnameValidLiveData.value = Pair(true, null)
        }
        isEverythingValid()
    }

    fun validateCountryOfBirth(input: String) {
        _isCountryOfBirthValidLiveData.value = input != "Select country of birth"
        isEverythingValid()
    }

    fun validateGender(input: Boolean?) {
        _isGenderValidLiveData.value = input == true
        isEverythingValid()
    }

    fun validateBirthDate(input: String?) {
        if (input.isNullOrBlank()) {
            _isBirthDateValidLiveData.value = Pair(false, R.string.empty_value)
        } else if (input.toIntOrNull() != null && input.toInt() > MAX_PATIENT_AGE) {
            _isBirthDateValidLiveData.value = Pair(false, R.string.age_out_of_bounds_message)
        } else {
            _isBirthDateValidLiveData.value = Pair(true, null)
        }
        isEverythingValid()
    }

    fun validateLegalConsent(input: Boolean?) {
        _isLegalConsentValidLiveData.value = input == true
        isEverythingValid()
    }

    private fun isEverythingValid() {
        _isPatientValidLiveData.value =
            _isNameValidLiveData.value?.first == true &&
                    _isSurnameValidLiveData.value?.first == true &&
                    _isCountryOfBirthValidLiveData.value == true &&
                    _isGenderValidLiveData.value == true &&
                    _isBirthDateValidLiveData.value?.first == true &&
                    _isLegalConsentValidLiveData.value == true
    }
}
