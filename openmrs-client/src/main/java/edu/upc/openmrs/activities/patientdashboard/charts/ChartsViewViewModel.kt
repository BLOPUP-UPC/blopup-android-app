package edu.upc.openmrs.activities.patientdashboard.charts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.model.Treatment
import edu.upc.blopup.model.Visit
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.api.repository.NewVisitRepository
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChartsViewViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val treatmentRepository: TreatmentRepository,
    private val visitRepository: NewVisitRepository
) :
    ViewModel() {
    private val _visits = MutableLiveData<ResultUiState<List<Visit>>>()
    val visits: LiveData<ResultUiState<List<Visit>>> get() = _visits

    private val _treatments = MutableLiveData<Result<Map<LocalDate, List<TreatmentAdherence>>>>()
    val treatments: LiveData<Result<Map<LocalDate, List<TreatmentAdherence>>>> get() = _treatments

    private val _visitsWithTreatments = MutableLiveData<ResultUiState<List<VisitWithAdherence>>>()
    val visitsWithTreatments: LiveData<ResultUiState<List<VisitWithAdherence>>> get() = _visitsWithTreatments

    fun fetchVisits(patientId: UUID) {
        viewModelScope.launch {
            _visits.value = ResultUiState.Loading
            _visits.value = ResultUiState.Success(visitRepository.getVisitsByPatientUuid(patientId))
        }
    }

    fun fetchVisitsWithTreatments(patientId: Int, patientUuiid: UUID) {
        viewModelScope.launch {
            _visitsWithTreatments.value = ResultUiState.Loading

            val patient: Patient = patientDAO.findPatientByID(patientId.toString())
            val visits = visitRepository.getVisitsByPatientUuid(patientUuiid)
            when(val treatments = treatmentRepository.fetchAllTreatments(patient)) {
                is Result.Success -> {
                    val adherenceByVisitId = adherenceByDate(treatments.data)
                    val visitsWithAdherence = visits
                        .sortedBy() { it.startDate }
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

    suspend fun fetchTreatments(patientId: Int) {
        val patient: Patient = patientDAO.findPatientByID(patientId.toString())

        when (val result = treatmentRepository.fetchAllTreatments(patient)) {
            is Result.Success -> {
                _treatments.value = Result.Success(treatmentsByAdherenceDate(result.data))
            }
            is Result.Error -> {
                _treatments.value = Result.Error(result.throwable)
            }
            else -> {}
        }
    }

    private fun treatmentsByAdherenceDate(treatments: List<Treatment>): Map<LocalDate, List<TreatmentAdherence>> {
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