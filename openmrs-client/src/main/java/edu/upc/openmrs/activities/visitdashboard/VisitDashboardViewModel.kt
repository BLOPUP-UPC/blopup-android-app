package edu.upc.openmrs.activities.visitdashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.bloodpressure.BloodPressureType
import edu.upc.blopup.bloodpressure.bloodPressureTypeFromEncounter
import edu.upc.openmrs.activities.BaseViewModel
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.Encounter
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.Visit
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.VISIT_ID
import edu.upc.sdk.utilities.ApplicationConstants.EncounterTypes.ENCOUNTER_TYPES_DISPLAYS
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class VisitDashboardViewModel @Inject constructor(
    private val visitDAO: VisitDAO,
    private val visitRepository: VisitRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<Visit>() {

    private val _bloodPressureType: MutableLiveData<BloodPressureType?> = MutableLiveData()
    val bloodPressureType: LiveData<BloodPressureType?> get() = _bloodPressureType

    private val visitId: Long = savedStateHandle[VISIT_ID]!!
    val visit: Visit?
        get() {
            val visitResult = result.value
            return if (visitResult is Result.Success<Visit>) visitResult.data else null
        }

    fun fetchCurrentVisit() {
        setLoading()
        addSubscription(visitDAO.getVisitByID(visitId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { visit ->
                    val filteredAndSortedEncounters = filterAndSortEncounters(visit.encounters)
                    visit.encounters = filteredAndSortedEncounters

                    val bpType = bloodPressureTypeFromEncounter(
                        filteredAndSortedEncounters.sortedBy { it.encounterDatetime }.last()
                    )

                    _bloodPressureType.value = bpType
                    setContent(visit)
                },
                { setError(it) }
            )
        )
    }

    fun endCurrentVisit(): LiveData<Boolean> {
        val endVisitResult = MutableLiveData<Boolean>()

        if (visit != null) {
            addSubscription(visitRepository.endVisit(visit)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { endVisitResult.value = true },
                    { endVisitResult.value = false }
                )
            )
        } else {
            endVisitResult.value = false
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