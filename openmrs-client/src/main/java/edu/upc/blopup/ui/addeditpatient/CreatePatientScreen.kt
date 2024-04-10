package edu.upc.blopup.ui.addeditpatient

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import edu.upc.R
import edu.upc.blopup.ui.dashboard.DashboardActivity
import edu.upc.blopup.ui.shared.components.LoadingSpinner
import edu.upc.blopup.ui.shared.components.SubmitButton
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardActivity
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ToastUtil

@Composable
fun CreatePatientScreen(
    activity: Activity,
    viewModel: CreatePatientViewModel = hiltViewModel()
) {
    val createPatientUiState = viewModel.createPatientUiState.collectAsState()

    CreatePatientForm(
        viewModel::isNameOrSurnameInvalidFormat,
        viewModel::createPatient,
        createPatientUiState.value,
        activity
    )
}

@Composable
fun CreatePatientForm(
    isNameOrSurnameInvalidFormat: (String) -> Boolean,
    createPatient: (String, String, String, String, String, String) -> Unit,
    createPatientUiState: CreatePatientResultUiState,
    activity: Activity,
) {
    var name by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var estimatedYears by remember { mutableStateOf("") }
    var countryOfBirth by remember { mutableStateOf("") }
    var legalConsentFile by remember { mutableStateOf("") }

    var isSubmitEnabled by remember { mutableStateOf(false) }


    fun checkAllFieldsFilled(): Boolean {
        return name.isNotBlank() &&
                familyName.isNotBlank() &&
                gender.isNotBlank() &&
                (dateOfBirth.isNotBlank() || estimatedYears.isNotBlank()) &&
                countryOfBirth.isNotBlank() &&
                legalConsentFile.isNotBlank()
    }

    DisposableEffect(
        name,
        familyName,
        gender,
        dateOfBirth,
        estimatedYears,
        countryOfBirth,
        legalConsentFile
    ) {
        isSubmitEnabled = checkAllFieldsFilled()
        onDispose { }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(25.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        FullNameSection(
            name,
            familyName,
            { name = it },
            { familyName = it },
            isNameOrSurnameInvalidFormat
        )

        GenderSection(gender) { gender = it }

        DateOfBirthSection(
            dateOfBirth,
            estimatedYears,
            { dateOfBirth = it },
            { estimatedYears = it })


        CountryOfBirthSection(countryOfBirth, { countryOfBirth = it }, activity)

        LegalConsentSection(activity, { legalConsentFile = it }, legalConsentFile)

        Column {
            when (createPatientUiState) {
                CreatePatientResultUiState.NotCreated -> {
                    SubmitButton(
                        title = R.string.action_submit,
                        onClickNext = {
                            createPatient(
                                name,
                                familyName,
                                dateOfBirth,
                                estimatedYears,
                                gender,
                                countryOfBirth
                            )
                        },
                        enabled = isSubmitEnabled
                    )
                }
                CreatePatientResultUiState.Error -> {
                    SubmitButton(
                        title = R.string.action_submit,
                        onClickNext = {
                            createPatient(
                                name,
                                familyName,
                                dateOfBirth,
                                estimatedYears,
                                gender,
                                countryOfBirth
                            )
                        },
                        enabled = isSubmitEnabled
                    )
                    ToastUtil.error(activity.getString(R.string.register_patient_error))
                }

                CreatePatientResultUiState.Loading -> {
                    LoadingSpinner(modifier = Modifier.padding(5.dp), color = R.color.allergy_orange)
                }

                is CreatePatientResultUiState.Success -> {
                    startPatientDashboardActivity(activity, createPatientUiState.data)
                }

            }
        }
    }
}

private fun startPatientDashboardActivity(context: Context, patient: Patient) {
    Intent(context, PatientDashboardActivity::class.java).apply {
        putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, patient.id)
        putExtra(ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE, patient.uuid)
        startActivity(context, this, null)
    }
}

@Composable
fun LanguagesDialog(
    context: Context,
    onLanguageSelected: (String) -> Unit,
    onDialogClose: () -> Unit
) {
    Dialog(onDismissRequest = { onDialogClose() }) {
        Column(
            Modifier
                .background(Color.White)
                .padding(10.dp)
        ) {
            LazyColumn {
                val languagesArray = context.resources.getStringArray(R.array.languages)
                items(languagesArray) {
                    Text(
                        text = it,
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(5.dp)
                            .clickable { onLanguageSelected(it); onDialogClose() })
                }
            }
        }
    }
}


@Preview
@Composable
fun CreatePatientPreview() {
    CreatePatientForm(
        { false },
        { _, _, _, _, _, _ -> },
        CreatePatientResultUiState.NotCreated,
        DashboardActivity()
    )
}

@Preview
@Composable
fun CreatePatientLoadingPreview() {
    CreatePatientForm(
        { false },
        { _, _, _, _, _, _ -> },
        CreatePatientResultUiState.Loading,
        DashboardActivity()
    )
}

@Preview
@Composable
fun LanguageDialogPreview() {
    LanguagesDialog(LocalContext.current, {}) {}
}