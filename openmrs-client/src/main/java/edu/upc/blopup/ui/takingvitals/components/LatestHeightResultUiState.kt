package edu.upc.blopup.ui.takingvitals.components

sealed interface LatestHeightResultUiState {
    data object Loading : LatestHeightResultUiState
    data object Error : LatestHeightResultUiState
    data class Success(val height: Int) : LatestHeightResultUiState
    data object NotFound : LatestHeightResultUiState
}
