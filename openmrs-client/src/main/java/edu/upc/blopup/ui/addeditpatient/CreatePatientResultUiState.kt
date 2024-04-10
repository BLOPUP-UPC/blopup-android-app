package edu.upc.blopup.ui.addeditpatient

import edu.upc.sdk.library.models.Patient

sealed interface CreatePatientResultUiState {

    data object Loading : CreatePatientResultUiState

    data object Error : CreatePatientResultUiState

    data class Success(val data: Patient) : CreatePatientResultUiState

    data object NotCreated : CreatePatientResultUiState

}
