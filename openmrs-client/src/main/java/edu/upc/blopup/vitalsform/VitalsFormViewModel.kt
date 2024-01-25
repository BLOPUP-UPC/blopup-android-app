package edu.upc.blopup.vitalsform

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.EncounterRepository
import edu.upc.sdk.library.api.repository.FormRepository
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.EncounterType
import edu.upc.sdk.library.models.Encountercreate
import edu.upc.sdk.library.models.Obscreate
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.Treatment
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.execute
import kotlinx.coroutines.launch
import org.joda.time.Instant
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class VitalsFormViewModel @Inject constructor(
    patientDAO: PatientDAO,
    private val formRepository: FormRepository,
    private val visitRepository: VisitRepository,
    private val encounterRepository: EncounterRepository,
    private val treatmentRepository: TreatmentRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<Unit>() {

    private val patientId: Long = savedStateHandle[PATIENT_ID_BUNDLE]!!
    private val encounterType: String = EncounterType.VITALS
    private val formName: String = "Vitals"

    val patient: Patient = patientDAO.findPatientByID(patientId.toString())

    private val _activeTreatments = MutableLiveData<kotlin.Result<List<Treatment>>>()
    val activeTreatments: LiveData<kotlin.Result<List<Treatment>>> get() = _activeTreatments

    suspend fun fetchActiveTreatments() {
        _activeTreatments.value = treatmentRepository.fetchAllActiveTreatments(patient)
    }

    fun getLastHeightFromVisits(): LiveData<Result<String>> {
        val resultLiveData = MutableLiveData<Result<String>>()
        val latestVisit = visitRepository.getLatestVisitWithHeight(patientId)
        latestVisit.ifPresent { visit ->
            resultLiveData.value = Result.Success(visit.getLatestHeight())
        }
        if (!latestVisit.isPresent) {
            resultLiveData.value = Result.Success("")
        }
        return resultLiveData
    }

    fun submitForm(
        vitals: List<Vital>,
        treatmentAdherence: Map<String, Boolean>
    ): LiveData<Result<Boolean>> {
        val resultLiveData = MutableLiveData<Result<Boolean>>()
        if (vitals.isEmpty()) {
            resultLiveData.value = Result.Error(IllegalArgumentException("Vitals list is empty"))
            return resultLiveData
        }
        val encounterCreate = Encountercreate()
        encounterCreate.patientId = patientId
        encounterCreate.observations = createObservationsFromVitals(vitals)

        return if (visitRepository.getActiveVisitByPatientId(patientId) == null) {
            try {
                val visit = visitRepository.startVisit(patient).execute()
                createRecords(encounterCreate, visit.uuid, treatmentAdherence)
            } catch (e: Exception) {
                resultLiveData.value = Result.Error(e.cause ?: e)
                return resultLiveData
            }
        } else {
            createRecords(encounterCreate, null, treatmentAdherence)
        }
    }

    private fun createObservationsFromVitals(vitals: List<Vital>): List<Obscreate> {
        val observations = mutableListOf<Obscreate>()

        for (vital in vitals) {
            if (vital.validate()) {
                observations += Obscreate().apply {
                    concept = vital.concept
                    value = vital.value
                    obsDatetime = Instant.now().toString()
                    person = patient.uuid
                }
            }
        }
        return observations
    }

    private fun createRecords(
        encounterCreate: Encountercreate,
        visitUuid: String?,
        treatmentAdherence: Map<String, Boolean>
    ): MutableLiveData<Result<Boolean>> {
        val resultLiveData = MutableLiveData<Result<Boolean>>()

        encounterCreate.patient = patient.uuid
        encounterCreate.encounterType = encounterType
        encounterCreate.formname = formName
        encounterCreate.formUuid = formRepository.fetchFormResourceByName(formName).execute().uuid

        addSubscription(
            encounterRepository.saveEncounter(encounterCreate)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    resultLiveData.value = it
                    if (resultLiveData.value is Result.Success) {
                        saveTreatmentAdherence(treatmentAdherence)
                    }
                    if ((resultLiveData.value is Result.Error) && (visitUuid != null))
                        visitRepository.deleteVisitByUuid(visitUuid)
                },
                    {
                        resultLiveData.value = Result.Error(it);
                        if (visitUuid != null) {
                            visitRepository.deleteVisitByUuid(visitUuid)
                        }
                    }
                )
        )

        return resultLiveData
    }

    private fun saveTreatmentAdherence(treatmentAdherence: Map<String, Boolean>) {
        viewModelScope.launch {
            treatmentRepository.saveTreatmentAdherence(treatmentAdherence, patient.uuid!!)
        }
    }
}
