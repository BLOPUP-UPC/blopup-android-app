package edu.upc.openmrs.activities.patientdashboard.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.OperationType.PatientFetching
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Treatment
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import javax.inject.Inject
import edu.upc.sdk.library.models.Result as OpenMrsResult

@HiltViewModel
class PatientDashboardDetailsViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val treatmentRepository: TreatmentRepository,
    savedStateHandle: SavedStateHandle
) : edu.upc.openmrs.activities.BaseViewModel<Patient>() {

    val activeTreatments: MutableLiveData<Result<List<Treatment>>> =
        MutableLiveData<Result<List<Treatment>>>()

    private val patientId: String = savedStateHandle.get(PATIENT_ID_BUNDLE)!!

    fun fetchPatientData() {
        setLoading(PatientFetching)
        val patient = patientDAO.findPatientByID(patientId)
        if (patient != null) setContent(patient, PatientFetching)
        else setError(IllegalStateException("Error fetching patient"), PatientFetching)
    }

    suspend fun fetchActiveTreatments(patient: Patient) {
        activeTreatments.value = treatmentRepository.fetchAllActiveTreatments(patient)
    }

    suspend fun refreshActiveTreatments() {
        if (result.value is OpenMrsResult.Success) {
            activeTreatments.value = treatmentRepository.fetchAllActiveTreatments(((result.value as OpenMrsResult.Success<Patient>).data))
        }
    }
}
