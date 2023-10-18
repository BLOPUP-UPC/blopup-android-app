package edu.upc.sdk.library.models

import edu.upc.sdk.library.models.OperationType.GeneralOperation

sealed class Result<out T> {
    data class Success<out T>(val data: T, val operationType: OperationType = GeneralOperation) : Result<T>()
    data class Error(val throwable: Throwable, val operationType: OperationType = GeneralOperation) : Result<Nothing>()
    class Loading(val operationType: OperationType = GeneralOperation) : Result<Nothing>()
}

enum class OperationType {
    GeneralOperation,
    PatientRegistering,
    PatientMerging,
    PatientFetching,
    PatientSearching,
    PatientSynchronizing,
    PatientVisitsFetching,
    PatientVisitStarting,
    LastViewedPatientsFetching,
}

enum class ResultType {
    PatientUpdateSuccess,
    PatientUpdateLocalSuccess,
    PatientUpdateError,
    EncounterSubmissionSuccess,
    EncounterSubmissionLocalSuccess,
    EncounterSubmissionError,
    EmailSentSuccess,
    EmailSentError,
    RecordingSuccess,
    RecordingError
}
