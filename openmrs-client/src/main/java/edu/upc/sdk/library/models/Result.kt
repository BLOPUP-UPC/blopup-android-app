package edu.upc.sdk.library.models

import edu.upc.sdk.library.models.OperationType.GeneralOperation

/**
 * This class is used to represent the result of an operation.
 * This class should be used to return data from repositories
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T, val operationType: OperationType = GeneralOperation) :
        Result<T>()

    data class Error(
        val throwable: Throwable,
        val operationType: OperationType = GeneralOperation
    ) : Result<Nothing>() {

        fun throwable() = this.throwable
    }

    // This state make no sense here???
    // Loading should be used in the ResultUiState
    class Loading(val operationType: OperationType = GeneralOperation) : Result<Nothing>()

}

@Deprecated("Avoid using Operation Type")
enum class OperationType {
    GeneralOperation,
    PatientRegistering,
    PatientFetching,
    PatientSynchronizing,
    PatientVisitsFetching,
    PatientVisitStarting,
    TreatmentUpdated}

@Deprecated("Use ResultUiState instead")
enum class ResultType {
    PatientUpdateSuccess,
    PatientUpdateLocalSuccess,
    PatientUpdateError,
    RecordingSuccess,
    RecordingError,
    FinalisedTreatmentSuccess,
    FinalisedTreatmentError,
    RemoveTreatmentSuccess,
    RemoveTreatmentError
}
