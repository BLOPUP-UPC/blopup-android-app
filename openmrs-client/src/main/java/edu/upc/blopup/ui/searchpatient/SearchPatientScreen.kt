package edu.upc.blopup.ui.searchpatient

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.models.Patient

@Composable
fun SearchPatientScreen(
    viewModel: SearchPatientViewModel = hiltViewModel()
) {

    val patientList by viewModel.patientListResultUiState.collectAsState()

    LaunchedEffect(true) {
        viewModel.getAllPatientsLocally()
    }

    when (patientList) {
        is ResultUiState.Success -> {
            LazyColumn {
                val patients = (patientList as ResultUiState.Success<List<Patient>>).data
                items(patients) { patient ->
                    Text(patient.contactNames[0].givenName + " " + (patient.contactNames[0].familyName?.get(0) ?: ""))
                }
            }
        }

        is ResultUiState.Loading -> {

        }

        is ResultUiState.Error -> {
        }
    }

}
