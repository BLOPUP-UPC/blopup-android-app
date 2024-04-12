package edu.upc.blopup.ui.takingvitals.screens

import edu.upc.blopup.model.Visit

sealed interface CreateVisitResultUiState {
    data object Loading : CreateVisitResultUiState
    data object Error : CreateVisitResultUiState
    data class Success(val visit: Visit) : CreateVisitResultUiState
    data object NotStarted : CreateVisitResultUiState
}
