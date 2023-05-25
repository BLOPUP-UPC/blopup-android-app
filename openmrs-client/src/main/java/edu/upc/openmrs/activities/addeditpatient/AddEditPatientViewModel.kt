package edu.upc.openmrs.activities.addeditpatient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.BuildConfig
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.openmrs.application.OpenMRS
import edu.upc.openmrs.utilities.FileUtils
import edu.upc.sdk.library.api.repository.ConceptRepository
import edu.upc.sdk.library.api.repository.PatientRepository
import edu.upc.sdk.library.api.repository.RecordingRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.ConceptAnswers
import edu.upc.sdk.library.models.OperationType.PatientRegistering
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.RecordingRequest
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.COUNTRIES_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.PatientValidator
import org.joda.time.DateTime
import rx.android.schedulers.AndroidSchedulers
import java.io.File
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class AddEditPatientViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val patientRepository: PatientRepository,
    private val conceptRepository: ConceptRepository,
    private val recordingRepository: RecordingRepository,
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
        private set

    var isPatientUnidentified = false
        set(value) {
            field = value
            patientValidator.isPatientUnidentified = value
        }
    var placesClient: PlacesClient? = null

    var dateHolder: DateTime? = null
    var capturedPhotoFile: File? = null

    init {
        // Initialize patient state
        val patientId: String? = savedStateHandle.get(PATIENT_ID_BUNDLE)
        val foundPatient = patientDAO.findPatientByID(patientId)
        if (foundPatient != null) {
            isUpdatePatient = true
            patient = foundPatient
        } else {
            resetPatient()
        }

        // Get available countries picker list
        val countriesList: List<String> = savedStateHandle.get(COUNTRIES_BUNDLE)!!

        // Initialize patient data validator
        patientValidator = PatientValidator(patient, isPatientUnidentified, countriesList)
    }

    fun resetPatient() {
        isUpdatePatient = false
        capturedPhotoFile = null
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

    fun saveLegalConsent(): LiveData<ResultType> {
        val result = MutableLiveData<ResultType>()

        patient.attributes?.forEach { attribute ->
            if (attribute.attributeType?.uuid == BuildConfig.LEGAL_CONSENT_ATTRIBUTE_TYPE_UUID) {

                val file =
                    File(FileUtils.getRootDirectory() + "/" + attribute.value)

                val recordingRequest =
                    RecordingRequest(attribute.value.toString(), file.readBytes())

                addSubscription(recordingRepository.saveRecording(recordingRequest)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            result.value = ResultType.RecordingSuccess
                            patient.isLegalConsent = true
                            patientDAO.updatePatient(patient)
                        },
                        { result.value = ResultType.RecordingError }
                    )
                )
            }
        }

        return result
    }

    fun fetchSimilarPatients() {
        if (!patientValidator.validate()) return
        if (isPatientUnidentified) {
            _similarPatientsLiveData.value = emptyList()
            return
        }
        setLoading()
        addSubscription(patientRepository.fetchSimilarPatients(patient)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _similarPatientsLiveData.value = it }
        )
    }

    fun fetchCausesOfDeath(): LiveData<ConceptAnswers> {
        val liveData = MutableLiveData<ConceptAnswers>()
        addSubscription(patientRepository.causeOfDeathGlobalConceptID
            .flatMap { conceptRepository.getConceptByUuid(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { causesOfDeath: ConceptAnswers -> liveData.value = causesOfDeath },
                { throwable -> liveData.value = ConceptAnswers() }
            )
        )
        return liveData
    }

    private fun registerPatient() {
        setLoading()
        addSubscription(patientRepository.registerPatient(patient)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    setContent(it, PatientRegistering)
                    saveLegalConsent()
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
                    saveLegalConsent()
                },
                { _patientUpdateLiveData.value = ResultType.PatientUpdateError }
            )
        )
    }
}
