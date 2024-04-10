package edu.upc.blopup.ui.dashboard

import edu.upc.blopup.model.Visit

sealed interface ActiveVisitResultUiState {

    data object Loading : ActiveVisitResultUiState

    data object Error : ActiveVisitResultUiState

    data class Success(val visit: Visit) : ActiveVisitResultUiState

    data object NotFound : ActiveVisitResultUiState

}
