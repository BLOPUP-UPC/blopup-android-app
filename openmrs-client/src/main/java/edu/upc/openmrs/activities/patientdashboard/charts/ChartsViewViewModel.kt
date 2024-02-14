package edu.upc.openmrs.activities.patientdashboard.charts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Treatment
import org.joda.time.format.DateTimeFormat
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
        treatmentRepository.fetchAllTreatments(patient).onSuccess {
            _treatments.value = Result.success(treatmentsByAdherenceDate(it))
        }.onFailure {
            _treatments.value = Result.failure(it)
        }
    }

    private fun treatmentsByAdherenceDate(treatments: List<Treatment>): Map<LocalDate, List<TreatmentAdherence>> {
        val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        return treatments.flatMap { treatment ->
            treatment.adherence.map {
                TreatmentAdherence(
                    treatment.medicationName,
                    treatment.medicationType,
                    it.value,
                    it.key
                )
            }
        }.sortedBy { it.adherence } .groupBy { it.date }
    }
}