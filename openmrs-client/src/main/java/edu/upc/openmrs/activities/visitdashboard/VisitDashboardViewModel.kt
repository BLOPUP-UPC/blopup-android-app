package edu.upc.openmrs.activities.visitdashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.bloodpressure.BloodPressureResult
import edu.upc.blopup.bloodpressure.bloodPressureTypeFromEncounter
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.EncounterRepository
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.library.models.Treatment
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_ID
import edu.upc.sdk.utilities.ApplicationConstants.EncounterTypes.ENCOUNTER_TYPES_DISPLAYS
import kotlinx.coroutines.launch
import org.joda.time.Instant
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class VisitDashboardViewModel @Inject constructor(
    private val visitDAO: VisitDAO,
    private val visitRepository: VisitRepository,
    private val treatmentRepository: TreatmentRepository,
    private val encounterRepository: EncounterRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<Visit>() {

    private val _bloodPressureType: MutableLiveData<BloodPressureResult?> = MutableLiveData()
    val bloodPressureType: LiveData<BloodPressureResult?> get() = _bloodPressureType

    private val _treatments: MutableLiveData<List<Treatment>> = MutableLiveData()
    val treatments: LiveData<List<Treatment>> get() = _treatments

    private val _treatmentOperationsLiveData = MutableLiveData<ResultType>()
    val treatmentOperationsLiveData: LiveData<ResultType> get() = _treatmentOperationsLiveData

    private val visitId: Long = savedStateHandle[VISIT_ID]!!
    val visit: Visit?
        get() {
            val visitResult = result.value
            return if (visitResult is Result.Success<Visit>) visitResult.data else null
        }

    fun fetchCurrentVisit() {
        setLoading()
        addSubscription(
            visitDAO.getVisitByID(visitId).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ visit ->
                    viewModelScope.launch { fetchActiveTreatments(visit.patient, visit) }
                    val filteredAndSortedEncounters = filterAndSortEncounters(visit.encounters)
                    visit.encounters = filteredAndSortedEncounters

                    val bpType = bloodPressureTypeFromEncounter(
                        filteredAndSortedEncounters.sortedBy { it.encounterDatetime }.last()
                    )

                    _bloodPressureType.value = bpType
                    setContent(visit)
                }, { setError(it) })
        )
    }

    fun endCurrentVisit(): LiveData<Result<Boolean>> {
        val endVisitResult = MutableLiveData<Result<Boolean>>()

        if (visit != null) {
            addSubscription(
                visitRepository.endVisit(visit)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        endVisitResult.value = Result.Success(it)
                    }, {
                        endVisitResult.value = Result.Error(it)
                    })
            )
        }
        return endVisitResult
    }

    fun filterAndSortEncounters(encounters: List<Encounter>): List<Encounter> {
        val possibleEncounterTypes = ENCOUNTER_TYPES_DISPLAYS.toHashSet()
        val displayableEncounters =
            encounters.filter { possibleEncounterTypes.contains(it.encounterType?.display) }
        return displayableEncounters.sortedBy { it.encounterDatetime }
    }

    private suspend fun fetchActiveTreatments(patient: Patient, visit: Visit) {
        try {
            val response = treatmentRepository.fetchActiveTreatmentsAtAGivenTime(patient, visit)
            _treatments.postValue(response)
        } catch (e: Exception) {
            _treatments.postValue(emptyList())
        }
    }

    suspend fun finaliseTreatment(treatment: Treatment) {
        treatment.inactiveDate = Instant.now()
        treatment.isActive = false

        val response = treatmentRepository.finalise(treatment)

        if(response == ResultType.FinalisedTreatmentSuccess) {
            visit?.let { fetchActiveTreatments(it.patient, it) }
            _treatmentOperationsLiveData.value = ResultType.FinalisedTreatmentSuccess
        } else {
            _treatmentOperationsLiveData.value = ResultType.FinalisedTreatmentError
        }
    }

    suspend fun removeTreatment(treatment: Treatment) {
        val response = encounterRepository.removeEncounter(treatment.treatmentUuid)

        if(response == ResultType.RemoveTreatmentSuccess) {
            visit?.let { fetchActiveTreatments(it.patient, it) }
            _treatmentOperationsLiveData.value = ResultType.RemoveTreatmentSuccess
        } else {
            _treatmentOperationsLiveData.value = ResultType.RemoveTreatmentError
        }
    }
}