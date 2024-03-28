package edu.upc.openmrs.activities.visitdashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.model.Treatment
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.DoctorRepository
import edu.upc.sdk.library.api.repository.EncounterRepository
import edu.upc.sdk.library.api.repository.NewVisitRepository
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.BloodPressureResult
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.library.models.bloodPressureTypeFromEncounter
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_UUID
import edu.upc.sdk.utilities.ApplicationConstants.EncounterTypes.ENCOUNTER_TYPES_DISPLAYS
import kotlinx.coroutines.launch
import org.joda.time.Instant
import rx.android.schedulers.AndroidSchedulers
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VisitDashboardViewModel @Inject constructor(
    private val visitDAO: VisitDAO,
    private val newVisitRepository: NewVisitRepository,
    private val treatmentRepository: TreatmentRepository,
    private val encounterRepository: EncounterRepository,
    private val doctorRepository: DoctorRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<Visit>() {

    private val _bloodPressureType: MutableLiveData<BloodPressureResult?> = MutableLiveData()
    val bloodPressureType: LiveData<BloodPressureResult?> get() = _bloodPressureType

    private val _treatments = MutableLiveData<Result<List<Treatment>>>()
    val treatments: LiveData<Result<List<Treatment>>> get() = _treatments

    private val _treatmentOperationsLiveData = MutableLiveData<ResultType>()
    val treatmentOperationsLiveData: LiveData<ResultType> get() = _treatmentOperationsLiveData

    private val _doctorHasBeenContacted: MutableLiveData<Boolean> = MutableLiveData(false)
    val doctorHasBeenContacted: LiveData<Boolean> get() = _doctorHasBeenContacted

    private val visitUuid: String = savedStateHandle[VISIT_UUID]!!
    val visit: Visit?
        get() {
            val visitResult = result.value
            return if (visitResult is Result.Success<Visit>) visitResult.data else null
        }

    fun doctorHasBeenContacted(value: Boolean) {
        _doctorHasBeenContacted.value = value
    }

    fun fetchCurrentVisit() {
        setLoading()
        val visitId = visitDAO.getVisitsIDByUUID(visitUuid).toBlocking().first()
        addSubscription(
            visitDAO.getVisitByID(visitId).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ visit ->
                    val lastVitalEncounter = filterLastVitalEncounter(visit.encounters)
                    visit.encounters = listOf(lastVitalEncounter)

                    val bpType = bloodPressureTypeFromEncounter(lastVitalEncounter)

                    _bloodPressureType.value = bpType
                    setContent(visit)
                    viewModelScope.launch { fetchActiveTreatments(visit.patient, visit) }
                }, { setError(it) })
        )
    }

    suspend fun endCurrentVisit(): LiveData<Result<Boolean>> {
        val endVisitResult = MutableLiveData<Result<Boolean>>()

        if (visit != null) {
            endVisitResult.value = Result.Success(newVisitRepository.endVisit(UUID.fromString(visit!!.uuid)))
        }
        return endVisitResult
    }

    fun filterLastVitalEncounter(encounters: List<Encounter>): Encounter {
        val possibleEncounterTypes = ENCOUNTER_TYPES_DISPLAYS.toHashSet()
        val displayableEncounters =
            encounters.filter { possibleEncounterTypes.contains(it.encounterType?.display) }
        return displayableEncounters.sortedBy { it.encounterDatetime }.last()
    }

    private suspend fun fetchActiveTreatments(patient: Patient, visit: Visit) {
        _treatments.value = treatmentRepository.fetchActiveTreatmentsAtAGivenTime(patient, visit)
    }

    suspend fun finaliseTreatment(treatment: Treatment) {
        treatment.inactiveDate = Instant.now()
        treatment.isActive = false

        val response = treatmentRepository.finalise(treatment)

        if (response.isSuccess) {
            visit?.let { fetchActiveTreatments(it.patient, it) }
            _treatmentOperationsLiveData.value = ResultType.FinalisedTreatmentSuccess
        } else {
            _treatmentOperationsLiveData.value = ResultType.FinalisedTreatmentError
        }
    }

    suspend fun removeTreatment(treatment: Treatment) {
        val response = encounterRepository.removeEncounter(treatment.treatmentUuid)

        if (response.isSuccess && _treatments.value is Result.Success) {
            _treatments.value =
                (_treatments.value as Result.Success<List<Treatment>>).data.toMutableList().apply {
                    remove(treatment)
                }.let { Result.Success(it) }
            _treatmentOperationsLiveData.value = ResultType.RemoveTreatmentSuccess
        } else {
            _treatmentOperationsLiveData.value = ResultType.RemoveTreatmentError
        }
    }

    suspend fun refreshTreatments() {
        visit?.let { fetchActiveTreatments(it.patient, it) }
    }

    suspend fun sendMessageToDoctor(message: String): kotlin.Result<Boolean> =
        doctorRepository.sendMessageToDoctor(message)
}