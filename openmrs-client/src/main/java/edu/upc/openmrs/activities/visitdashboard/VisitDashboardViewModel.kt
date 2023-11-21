package edu.upc.openmrs.activities.visitdashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.bloodpressure.BloodPressureResult
import edu.upc.blopup.bloodpressure.bloodPressureTypeFromEncounter
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_ID
import edu.upc.sdk.utilities.ApplicationConstants.EncounterTypes.ENCOUNTER_TYPES_DISPLAYS
import rx.android.schedulers.AndroidSchedulers
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class VisitDashboardViewModel @Inject constructor(
    private val visitDAO: VisitDAO,
    private val visitRepository: VisitRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<Visit>() {

    private val _bloodPressureType: MutableLiveData<BloodPressureResult?> = MutableLiveData()
    val bloodPressureType: LiveData<BloodPressureResult?> get() = _bloodPressureType

    private val visitId: Long = savedStateHandle[VISIT_ID]!!
    val visit: Visit?
        get() {
            val visitResult = result.value
            return if (visitResult is Result.Success<Visit>) visitResult.data else null
        }

    fun fetchCurrentVisit() {
        setLoading()
        addSubscription(visitDAO.getVisitByID(visitId).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ visit ->
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

    fun endCurrentVisit(): LiveData<ResultType> {
        val endVisitResult = MutableLiveData<ResultType>()

        if (visit != null) {
            addSubscription(
                visitRepository.endVisit(visit)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (it) {
                            endVisitResult.value = ResultType.Success
                        }
                    }, {
                        endVisitResult.value = if (it is UnknownHostException) ResultType.NoInternetError
                        else ResultType.Error
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
}