package edu.upc.blopup.ui

sealed interface ResultUiState {

    data object Loading : ResultUiState

    data object Error : ResultUiState

    data class Success<T>(val data: T) : ResultUiState

}
