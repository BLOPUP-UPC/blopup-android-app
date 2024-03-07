package edu.upc.blopup.ui

sealed interface ResultUiState<out T> {

    data object Loading : ResultUiState<Nothing>

    data object Error : ResultUiState<Nothing>

    data class Success<out T>(val data: T) : ResultUiState<T>

}
