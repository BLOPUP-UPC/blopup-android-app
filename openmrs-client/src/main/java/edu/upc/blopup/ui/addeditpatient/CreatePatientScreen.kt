package edu.upc.blopup.ui.addeditpatient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.upc.R
import edu.upc.blopup.ui.shared.components.LoadingSpinner
import edu.upc.blopup.ui.shared.components.SubmitButton
import edu.upc.sdk.utilities.ToastUtil

@Composable
fun CreatePatientScreen(
    navigateToPatientDashboard: (Long, String) -> Unit,
    isFormWithSomeInput: () -> Unit,
    askLegalConsentPermission: () -> Unit,
    viewModel: CreatePatientViewModel = hiltViewModel()
) {
    val createPatientUiState = viewModel.createPatientUiState.collectAsState()

    LaunchedEffect(true) {
        askLegalConsentPermission()
    }

    CreatePatientForm(
        viewModel::isNameOrSurnameInvalidFormat,
        viewModel::createPatient,
        createPatientUiState.value,
        navigateToPatientDashboard,
        viewModel::isValidBirthDate,
        isFormWithSomeInput,
    )

}

@Composable
fun CreatePatientForm(
    isNameOrSurnameInvalidFormat: (String) -> Boolean,
    createPatient: (String, String, String, String, String, String, String) -> Unit,
    createPatientUiState: CreatePatientResultUiState,
    navigateToPatientDashboard: (Long, String) -> Unit,
    isBirthDateValidRange: (String) -> Boolean,
    isFormWithSomeInput: () -> Unit,
) {
    val context = LocalContext.current
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
        if (name.isNotEmpty() || familyName.isNotEmpty() || dateOfBirth.isNotEmpty() || estimatedYears.isNotEmpty() || countryOfBirth.isNotEmpty() || legalConsentFile.isNotEmpty()) {
            isFormWithSomeInput()
        }
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
            { estimatedYears = it },
            isBirthDateValidRange
        )


        CountryOfBirthSection(countryOfBirth, { countryOfBirth = it }, context)

        LegalConsentSection(context, { legalConsentFile = it }, legalConsentFile)

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
                                countryOfBirth,
                                legalConsentFile
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
                                countryOfBirth,
                                legalConsentFile
                            )
                        },
                        enabled = isSubmitEnabled
                    )
                    ToastUtil.error(context.getString(R.string.register_patient_error))
                }

                CreatePatientResultUiState.Loading -> {
                    LoadingSpinner(
                        modifier = Modifier.padding(5.dp),
                        color = R.color.allergy_orange
                    )
                }

                is CreatePatientResultUiState.Success -> {
                    navigateToPatientDashboard(createPatientUiState.data.id!!, createPatientUiState.data.uuid!!)
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
        { _, _, _, _, _, _, _ -> },
        CreatePatientResultUiState.NotCreated,
        {_, _ -> },
        { true },
    ) {}
}

@Preview
@Composable
fun CreatePatientLoadingPreview() {
    CreatePatientForm(
        { false },
        { _, _, _, _, _, _, _ -> },
        CreatePatientResultUiState.Loading,
        {_, _ -> },
        { true },
    ) { }
}