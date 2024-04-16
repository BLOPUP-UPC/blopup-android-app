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


    suspend fun getAllPatientsLocally() {
        try {
            when (val response = patientRepositoryCoroutines.getAllPatientsLocally()) {
                is Result.Success -> _patientListResultUiState.value = ResultUiState.Success(response.data)
                else -> _patientListResultUiState.value = ResultUiState.Error
            }
        } catch (e: Exception) {
            _patientListResultUiState.value = ResultUiState.Error
        }
    }

}