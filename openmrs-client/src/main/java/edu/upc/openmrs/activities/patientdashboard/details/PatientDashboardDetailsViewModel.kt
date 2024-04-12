package edu.upc.openmrs.activities.patientdashboard.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.model.Treatment
import edu.upc.sdk.library.OpenMRSLogger
import edu.upc.sdk.library.api.repository.PatientRepositoryCoroutines
import edu.upc.sdk.library.api.repository.TreatmentRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.OperationType.PatientFetching
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientDashboardDetailsViewModel @Inject constructor(
    private val patientDAO: PatientDAO,
    private val treatmentRepository: TreatmentRepository,
    private val patientRepositoryCoroutines: PatientRepositoryCoroutines,
    private val logger: OpenMRSLogger,
    savedStateHandle: SavedStateHandle
) : edu.upc.openmrs.activities.BaseViewModel<Patient>() {

    val activeTreatments: MutableLiveData<Result<List<Treatment>>> =
        MutableLiveData<Result<List<Treatment>>>()

    private val patientId: String = savedStateHandle[PATIENT_ID_BUNDLE]!!
    private val patientUuid: String = savedStateHandle[PATIENT_UUID_BUNDLE]!!

    fun fetchPatientData() {
        viewModelScope.launch {
            setLoading(PatientFetching)
            val patient = patientDAO.findPatientByID(patientId)
            if (patient != null)
                setContent(patient, PatientFetching)
            else
                setError(IllegalStateException("Error fetching patient"), PatientFetching)

            try {
                val remotePatient = patientRepositoryCoroutines.downloadPatientByUuid(patientUuid)
                patientDAO.updatePatient(patientId.toLong(), remotePatient)
            } catch (e: Exception) {
                logger.e("Failed to download patient", e)
            }
        }
    }

    suspend fun fetchActiveTreatments(patient: Patient) {
        activeTreatments.value = treatmentRepository.fetchAllActiveTreatments(patient)
    }

    suspend fun refreshActiveTreatments() {
        if (result.value is Result.Success) {
            activeTreatments.value = treatmentRepository.fetchAllActiveTreatments(((result.value as Result.Success<Patient>).data))
        }
    }
}
