package edu.upc.sdk.library.models

import edu.upc.sdk.library.models.OperationType.GeneralOperation

sealed class Result<out T> {
    data class Success<out T>(val data: T, val operationType: OperationType = GeneralOperation) :
        Result<T>()

     class Error(
        val throwable: Throwable,
        val operationType: OperationType = GeneralOperation
    ) : Result<Nothing>() {

        fun throwable() = this.throwable
    }



    class Loading(val operationType: OperationType = GeneralOperation) : Result<Nothing>()

}

enum class OperationType {
    GeneralOperation,
    PatientRegistering,
    PatientFetching,
    PatientSynchronizing,
    PatientVisitsFetching,
    PatientVisitStarting,
}

enum class ResultType {
    Success,
    PatientUpdateSuccess,
    PatientUpdateLocalSuccess,
    PatientUpdateError,
    EncounterSubmissionSuccess,
    EncounterSubmissionError,
    EmailSentSuccess,
    EmailSentError,
    RecordingSuccess,
    RecordingError,
    NoInternetError,
    Error
}
