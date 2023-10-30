package edu.upc.openmrs.activities.addeditpatient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.R
import edu.upc.blopup.RecordingHelper
import edu.upc.blopup.toggles.check
import edu.upc.blopup.toggles.showPatientConsentToggle
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.LegalConsent
import edu.upc.sdk.library.models.OperationType.PatientRegistering
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.PatientValidator
import edu.upc.sdk.utilities.StringUtils
import org.joda.time.DateTime
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class AddEditPatientViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val patientRepository: PatientRepository,
    private val recordingHelper: RecordingHelper,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<Patient>() {

    private val _similarPatientsLiveData = MutableLiveData<List<Patient>>()
    val similarPatientsLiveData: LiveData<List<Patient>> get() = _similarPatientsLiveData

    private val _patientUpdateLiveData = MutableLiveData<ResultType>()
    val patientUpdateLiveData: LiveData<ResultType> get() = _patientUpdateLiveData

    private val _isNameValidLiveData = MutableLiveData<Pair<Boolean, Int?>>()
    val isNameValidLiveData: LiveData<Pair<Boolean, Int?>> get() = _isNameValidLiveData

    private val _isSurnameValidLiveData = MutableLiveData<Pair<Boolean, Int?>>()
    val isSurnameValidLiveData: LiveData<Pair<Boolean, Int?>> get() = _isSurnameValidLiveData

    private val _isCountryOfBirthLiveData = MutableLiveData<Boolean>()
    val isCountryOfBirthLiveData: LiveData<Boolean> get() = _isCountryOfBirthLiveData

    private val _isGenderLiveData = MutableLiveData<Boolean>()
    val isGenderLiveData: LiveData<Boolean> get() = _isGenderLiveData

    private val _isBirthDateLiveData = MutableLiveData<Boolean>()
    val isBirthDateLiveData: LiveData<Boolean> get() = _isBirthDateLiveData

    private val _isLegalConsentLiveData = MutableLiveData<Boolean>()
    val isLegalConsentLiveData: LiveData<Boolean> get() = _isLegalConsentLiveData

    private val _isPatientValidLiveData = MutableLiveData<Boolean>()
    val isPatientValidLiveData: LiveData<Boolean> get() = _isPatientValidLiveData

    var patientValidator: PatientValidator

    var isUpdatePatient = false
        private set
    lateinit var patient: Patient

    var isLegalRecordingPresent = false
        set(value) {
            field = value
            patientValidator.isLegalRecordingPresent = value
        }

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

        // Initialize patient data validator
        patientValidator = PatientValidator(patient, isLegalRecordingPresent)
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
                        showPatientConsentToggle.check(onToggleEnabled = {
                            recordingHelper.saveLegalConsent(LegalConsent().apply {
                                val patientIdentifier = patient.identifier.identifier
                                if (patientIdentifier != null) {
                                    this.patientIdentifier = patientIdentifier
                                    this.filePath = legalConsentFileName
                                }
                            })
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
        _isCountryOfBirthLiveData.value = input != "Select country of birth"
        isEverythingValid()
    }

    fun validateGender(input: Boolean?) {
        _isGenderLiveData.value = input == true
        isEverythingValid()
    }

    fun validateBirthDate(input: String?) {
        _isBirthDateLiveData.value = !input.isNullOrBlank()
        isEverythingValid()
    }

    fun validateLegalConsent(input: Boolean?) {
        _isLegalConsentLiveData.value = input == true
        isEverythingValid()
    }

    private fun isEverythingValid() {
        _isPatientValidLiveData.value =
            _isNameValidLiveData.value?.first == true &&
                    _isSurnameValidLiveData.value?.first == true &&
                    _isCountryOfBirthLiveData.value == true &&
                    _isGenderLiveData.value == true &&
                    _isBirthDateLiveData.value == true &&
                    _isLegalConsentLiveData.value == true
    }
}
