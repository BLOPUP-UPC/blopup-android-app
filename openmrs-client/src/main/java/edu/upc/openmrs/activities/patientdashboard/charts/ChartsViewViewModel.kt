package edu.upc.openmrs.activities.patientdashboard.charts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import javax.inject.Inject

@HiltViewModel
class ChartsViewViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val treatmentRepository: TreatmentRepository
) :
    ViewModel() {
    private val _treatments = MutableLiveData<Result<List<edu.upc.sdk.library.models.Treatment>>>()
    val treatments: LiveData<Result<List<edu.upc.sdk.library.models.Treatment>>> get() = _treatments

    suspend fun fetchTreatments(patientId: Int) {
        try {
            val patient: Patient = patientDAO.findPatientByID(patientId.toString())
            val result = treatmentRepository.fetchAllTreatments(patient)
            _treatments.value = result
        } catch (e: Exception) {
            _treatments.value = Result.failure(e)
        }
    }
}