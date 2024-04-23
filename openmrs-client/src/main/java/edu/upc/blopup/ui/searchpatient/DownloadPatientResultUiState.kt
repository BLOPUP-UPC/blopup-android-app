package edu.upc.blopup.ui.searchpatient

import edu.upc.sdk.library.models.Patient
import java.util.UUID

sealed interface DownloadPatientResultUiState {

    data class Error(val patientId: UUID) : DownloadPatientResultUiState

    data class Success(val data: Patient) : DownloadPatientResultUiState

    data object NotStarted : DownloadPatientResultUiState
}
