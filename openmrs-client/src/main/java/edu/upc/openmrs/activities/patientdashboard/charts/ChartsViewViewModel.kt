package edu.upc.openmrs.activities.patientdashboard.charts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.model.Treatment
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ChartsViewViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val treatmentRepository: TreatmentRepository
) :
    ViewModel() {
    private val _treatments = MutableLiveData<Result<Map<LocalDate, List<TreatmentAdherence>>>>()
    val treatments: LiveData<Result<Map<LocalDate, List<TreatmentAdherence>>>> get() = _treatments

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
}