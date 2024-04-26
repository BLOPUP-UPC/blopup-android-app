package edu.upc.openmrs.activities.patientdashboard.charts

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.R
import edu.upc.blopup.model.MedicationType
import edu.upc.blopup.model.Treatment
import edu.upc.blopup.model.Visit
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.api.repository.VisitRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.DateUtils.toLocalDate
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BloodPressureChartViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val treatmentRepository: TreatmentRepository,
    private val visitRepository: VisitRepository
) :
    ViewModel() {
    private val _visitsWithTreatments = MutableLiveData<ResultUiState<List<VisitWithAdherence>>>()
    val visitsWithTreatments: LiveData<ResultUiState<List<VisitWithAdherence>>> get() = _visitsWithTreatments

    fun fetchVisitsWithTreatments(patientId: Int, patientUuiid: UUID) {
        viewModelScope.launch {
            _visitsWithTreatments.value = ResultUiState.Loading

            val patient: Patient = patientDAO.findPatientByID(patientId.toString())
            val visits = visitRepository.getVisitsByPatientUuid(patientUuiid)
            when(val treatments = treatmentRepository.fetchAllTreatments(UUID.fromString(patient.uuid))) {
                is Result.Success -> {
                    val adherenceByVisitId = adherenceByDate(treatments.data)
                    val visitsWithAdherence = visits
                        .sortedBy { it.startDate }
                        .reversed()
                        .distinctBy { it.startDate.toLocalDate() }
                        .map { visit ->
                            VisitWithAdherence(
                                visit,
                                adherenceByVisitId[visit.startDate.toLocalDate()] ?: emptyList()
                            )
                        }
                        .reversed()
                    _visitsWithTreatments.value = ResultUiState.Success(visitsWithAdherence)
                }
                is Result.Error -> {
                    _visitsWithTreatments.value = ResultUiState.Error
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun adherenceByDate(treatments: List<Treatment>): Map<LocalDate, List<TreatmentAdherence>> {
        return treatments.flatMap { treatment ->
            treatment.adherence.map {
                TreatmentAdherence(
                    treatment.medicationName,
                    treatment.medicationType,
                    it.value,
                    it.key
                )
            }
        }.sortedBy { it.adherence }.groupBy { it.date }.toSortedMap(reverseOrder())
    }
}

data class VisitWithAdherence(
    val visit: Visit,
    val adherence: List<TreatmentAdherence>
)

data class TreatmentAdherence(
    val medicationName: String,
    var medicationType: Set<MedicationType>,
    val adherence: Boolean,
    val date: LocalDate
)

fun TreatmentAdherence.medicationTypeToString(context: Context): String {
    return medicationType.joinToString(separator = " • ") { context.getString(it.label) }
}

fun TreatmentAdherence.icon(): Int {
    return if (adherence) R.drawable.ic_tick else R.drawable.ic_cross
}

fun List<TreatmentAdherence>.followTreatments(): FollowTreatments {
    val trueValues = this.filter { it.adherence }.size

    return when {
        this.isEmpty() -> FollowTreatments.NO_INFO
        trueValues == 0 -> FollowTreatments.FOLLOW_NONE
        trueValues == this.size -> FollowTreatments.FOLLOW_ALL
        else -> FollowTreatments.FOLLOW_SOME
    }
}

enum class FollowTreatments {
    NO_INFO, FOLLOW_ALL, FOLLOW_SOME, FOLLOW_NONE
}