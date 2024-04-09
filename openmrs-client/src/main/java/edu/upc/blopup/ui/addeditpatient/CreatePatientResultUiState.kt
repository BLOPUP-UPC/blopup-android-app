package edu.upc.blopup.ui.addeditpatient

sealed interface CreatePatientResultUiState {

    data object Loading : CreatePatientResultUiState

    data object Error : CreatePatientResultUiState

    data class Success(val data: Long) : CreatePatientResultUiState

    data object NotCreated : CreatePatientResultUiState

}
