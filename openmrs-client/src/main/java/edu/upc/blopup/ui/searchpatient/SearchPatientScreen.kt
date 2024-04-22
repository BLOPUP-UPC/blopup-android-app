package edu.upc.blopup.ui.searchpatient

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.upc.blopup.ui.ResultUiState
import edu.upc.blopup.ui.shared.components.LoadingSpinner
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.PatientIdentifier
import edu.upc.sdk.library.models.PersonName
import edu.upc.sdk.utilities.DateUtils.convertTime

@Composable
fun SearchPatientScreen(
    startPatientDashboardActivity: (patientId: Long, patientUuid: String) -> Unit,
    searchQuery: String,
    viewModel: SearchPatientViewModel = hiltViewModel()
) {

    val localPatientList by viewModel.patientListResultUiState.collectAsState()
    val remotePatientList by viewModel.remotePatientListResultUiState.collectAsState()


    LaunchedEffect(true) {
        viewModel.getAllPatientsLocally()
    }

    LaunchedEffect(searchQuery) {
        viewModel.getAllPatientsRemotely(searchQuery)
    }

    SyncedPatients(
        patientList = if (searchQuery.isEmpty()) localPatientList else remotePatientList,
        startPatientDashboardActivity = startPatientDashboardActivity,
    )
}

@Composable
fun SyncedPatients(
    patientList: ResultUiState<List<Patient>>,
    startPatientDashboardActivity: (patientId: Long, patientUuid: String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        when (patientList) {
            is ResultUiState.Success -> {
                LazyColumn {
                    items(patientList.data) { patient ->
                        PatientCard(patient, startPatientDashboardActivity)
                    }
                }
            }

            is ResultUiState.Loading -> {
                LoadingSpinner(Modifier.padding(16.dp))
            }

            is ResultUiState.Error -> {
            }
        }
    }
}

@Composable
fun PatientCard(
    patient: Patient,
    startPatientDashboardActivity: (patientId: Long, patientUuid: String) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 5.dp, vertical = 2.dp)
            .clickable {
                startPatientDashboardActivity(patient.id!!, patient.uuid!!)
            },
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(0.dp, Color.LightGray),
        colors = CardColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = Color.Black,
            disabledContentColor = Color.Black
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            Text(
                text = patient.display.toString(),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Row {
                Text(
                    text = ("# " + patient.identifiers[0].identifier),
                    color = Color.Gray,
                    modifier = Modifier.padding(end = 10.dp),
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = convertTime(convertTime(patient.birthdate) ?: 0L), color = Color.Gray)
            }

        }

    }
}

@Preview
@Composable
fun PreviewSearchPatientScreen() {
    SyncedPatients(ResultUiState.Success(listOf(Patient().apply {
        identifiers = listOf(PatientIdentifier().apply {
            id = 10007L
            identifier = "10007"
        })
        names = listOf(PersonName().apply {
            givenName = "Jane"
            familyName = "Doe"
        })
        birthdate = "1980-01-20T00:00:00.000+0000"
        birthdateEstimated = false
    }))) { _, _ -> }
}
