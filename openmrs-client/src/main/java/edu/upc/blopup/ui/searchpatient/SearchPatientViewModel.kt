package edu.upc.blopup.ui.searchpatient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.api.repository.PatientRepositoryCoroutines
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
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

    private var _retrievePatientResult: MutableStateFlow<DownloadPatientResultUiState> =
        MutableStateFlow(DownloadPatientResultUiState.NotStarted)
    val retrievePatientResult: StateFlow<DownloadPatientResultUiState> =
        _retrievePatientResult.asStateFlow()


    suspend fun getAllPatientsLocally() {
        when (val response = patientRepositoryCoroutines.getAllPatientsLocally()) {
            is Result.Success -> _patientListResultUiState.value = ResultUiState.Success(response.data)
            else -> _patientListResultUiState.value = ResultUiState.Error
        }
    }

    suspend fun getAllPatientsRemotely(query: String) {
        when (val response = patientRepositoryCoroutines.findPatients(query)) {
            is Result.Success -> _remotePatientListResultUiState.value =
                ResultUiState.Success(response.data)

            else -> _remotePatientListResultUiState.value = ResultUiState.Error
        }
    }

    fun retrieveOrDownloadPatient(patientUuid: UUID) {
        viewModelScope.launch {
            try {
                val patient = patientRepositoryCoroutines.downloadPatientByUuid(patientUuid.toString())

                var localDBPatient = patientRepositoryCoroutines.findPatientByUUID(patientUuid.toString())
                if (localDBPatient == null) {
                    localDBPatient = patientRepositoryCoroutines.savePatientLocally(patient)
                }
                _retrievePatientResult.value = DownloadPatientResultUiState.Success(localDBPatient)
            } catch (e: Exception) {
                _retrievePatientResult.value = DownloadPatientResultUiState.Error(patientUuid)
            }
        }
    }

    fun deletePatientLocally(patientId: UUID) {
        viewModelScope.launch {
            patientRepositoryCoroutines.deletePatient(patientId)
            getAllPatientsLocally()
        }
    }

}