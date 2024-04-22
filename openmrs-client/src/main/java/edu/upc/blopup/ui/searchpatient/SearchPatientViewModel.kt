package edu.upc.blopup.ui.searchpatient

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.api.repository.PatientRepositoryCoroutines
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SearchPatientViewModel @Inject constructor(
    private val patientRepositoryCoroutines: PatientRepositoryCoroutines,
) : ViewModel() {

    private val _patientListResultUiState: MutableStateFlow<ResultUiState<List<Patient>>> =
        MutableStateFlow(ResultUiState.Loading)
    var patientListResultUiState: StateFlow<ResultUiState<List<Patient>>> =
        _patientListResultUiState.asStateFlow()

    private val _remotePatientListResultUiState: MutableStateFlow<ResultUiState<List<Patient>>> =
        MutableStateFlow(ResultUiState.Loading)
    var remotePatientListResultUiState: StateFlow<ResultUiState<List<Patient>>> =
        _remotePatientListResultUiState.asStateFlow()


    suspend fun getAllPatientsLocally() {
        when (val response = patientRepositoryCoroutines.getAllPatientsLocally()) {
            is Result.Success -> _patientListResultUiState.value =
                ResultUiState.Success(response.data)

            else -> _patientListResultUiState.value = ResultUiState.Error
        }
    }

    suspend fun getAllPatientsRemotely(query: String) {
        when (val response = patientRepositoryCoroutines.newFindPatients(query)) {
            is Result.Success -> _remotePatientListResultUiState.value =
                ResultUiState.Success(response.data)

            else -> _remotePatientListResultUiState.value = ResultUiState.Error
        }
    }

    suspend fun retrieveOrDownloadPatient(patientUuid: String?): kotlin.Result<Patient?> {
        return try {
            val patient = patientRepositoryCoroutines.downloadPatientByUuid(patientUuid!!)

            if (patient.names.isEmpty()) {
                kotlin.Result.success(null)
            } else {
                var localDBPatient = patientRepositoryCoroutines.findPatientByUUID(patientUuid)
                if (localDBPatient == null) {
                    localDBPatient = patientRepositoryCoroutines.savePatientLocally(patient)
                }
                kotlin.Result.success(localDBPatient)
            }
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

}