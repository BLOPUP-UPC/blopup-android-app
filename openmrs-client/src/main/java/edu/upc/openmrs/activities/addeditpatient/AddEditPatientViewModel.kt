package edu.upc.openmrs.activities.addeditpatient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.RecordingHelper
import edu.upc.blopup.toggles.check
import edu.upc.blopup.toggles.showPatientConsentToggle
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.ConceptRepository
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.ConceptAnswers
import edu.upc.sdk.library.models.LegalConsent
import edu.upc.sdk.library.models.OperationType.PatientRegistering
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.PatientValidator
import org.joda.time.DateTime
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class AddEditPatientViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val patientRepository: PatientRepository,
    private val conceptRepository: ConceptRepository,
    private val recordingHelper: RecordingHelper,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<Patient>() {

    private val _similarPatientsLiveData = MutableLiveData<List<Patient>>()
    val similarPatientsLiveData: LiveData<List<Patient>> get() = _similarPatientsLiveData

    private val _patientUpdateLiveData = MutableLiveData<ResultType>()
    val patientUpdateLiveData: LiveData<ResultType> get() = _patientUpdateLiveData

    var patientValidator: PatientValidator

    var isUpdatePatient = false
        private set
    lateinit var patient: Patient

    var isLegalRecordingPresent = false
        set(value) {
            field = value
            patientValidator.isLegalRecordingPresent = value
        }
    var placesClient: PlacesClient? = null

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
        if (!patientValidator.validate()) return
        if (isUpdatePatient) updatePatient()
        else {
            registerPatient()
        }

    }

    fun fetchSimilarPatients() {
        if (!patientValidator.validate()) return
        setLoading()
        addSubscription(patientRepository.fetchSimilarPatients(patient)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _similarPatientsLiveData.value = it }
        )
    }

    private fun registerPatient() {
        setLoading()
        addSubscription(patientRepository.registerPatient(patient)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    setContent(it, PatientRegistering)
                    showPatientConsentToggle.check(onToggleEnabled = {
                        recordingHelper.saveLegalConsent(LegalConsent().apply {
                            val patientIdentifier = patient.identifier.identifier
                            if(patientIdentifier != null){
                                this.patientIdentifier = patientIdentifier
                                this.filePath = legalConsentFileName
                            }
                        })
                    })
                },
                { setError(it, PatientRegistering) }
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
}
