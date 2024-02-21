package edu.upc.blopup.ui.takingvitals

sealed interface CreateVisitResultUiState {

    data object Loading : CreateVisitResultUiState

    data object Error : CreateVisitResultUiState

    data object Success : CreateVisitResultUiState
}
