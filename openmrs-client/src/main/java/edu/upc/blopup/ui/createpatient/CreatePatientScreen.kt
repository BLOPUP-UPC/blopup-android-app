package edu.upc.blopup.ui.createpatient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.upc.R
import edu.upc.blopup.model.Gender
import edu.upc.blopup.ui.createpatient.components.CountryOfBirthDialog
import edu.upc.blopup.ui.createpatient.components.CountryOfBirthField
import edu.upc.blopup.ui.createpatient.components.DateOfBirthSection
import edu.upc.blopup.ui.createpatient.components.LegalConsentSection
import edu.upc.blopup.ui.shared.components.LoadingSpinner
import edu.upc.blopup.ui.shared.components.SubmitButton
import edu.upc.openmrs.activities.editpatient.countryofbirth.Country
import edu.upc.sdk.utilities.ToastUtil

@Composable
fun CreatePatientScreen(
    navigateToPatientDashboard: (Long, String) -> Unit,
    onFormWithSomeInput: (Boolean) -> Unit,
    askLegalConsentPermission: () -> Unit,
    getStringByResourceId: (Int) -> String,
    getCountryLabel: (Country) -> String,
    getTextInLanguageSelected: (String, Int) -> String,
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
        viewModel::isInvalidBirthDate,
        onFormWithSomeInput,
        getStringByResourceId,
        getCountryLabel,
        getTextInLanguageSelected
    )

}

@Composable
fun CreatePatientForm(
    isNameOrSurnameInvalidFormat: (String) -> Boolean,
    createPatient: (String, String, String, String, String, String, String) -> Unit,
    createPatientUiState: CreatePatientResultUiState,
    navigateToPatientDashboard: (Long, String) -> Unit,
    isBirthDateValidRange: (String) -> Boolean,
    onFormWithSomeInput: (Boolean) -> Unit,
    getStringByResourceId: (Int) -> String,
    getCountryLabel: (Country) -> String,
    getTextInLanguageSelected: (String, Int) -> String,
) {

    var name by rememberSaveable { mutableStateOf("") }
    var familyName by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("") }
    var dateOfBirth by rememberSaveable { mutableStateOf("") }
    var estimatedYears by rememberSaveable { mutableStateOf("") }
    var countryOfBirth by rememberSaveable { mutableStateOf("") }
    var legalConsentFile by rememberSaveable { mutableStateOf("") }

    var isSubmitEnabled by rememberSaveable { mutableStateOf(false) }

    isSubmitEnabled = checkAllFieldsFilled(name, familyName, gender, dateOfBirth, estimatedYears, countryOfBirth, legalConsentFile)

    onFormWithSomeInput(isFormWithSomeInput(name, familyName, dateOfBirth, estimatedYears, countryOfBirth, legalConsentFile))


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


        CountryOfBirthSection(countryOfBirth, { countryOfBirth = it }, getCountryLabel)

        LegalConsentSection(
            { legalConsentFile = it },
            legalConsentFile,
            getStringByResourceId,
            getTextInLanguageSelected
        )

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
                    ToastUtil.error(getStringByResourceId(R.string.register_patient_error))
                }

                CreatePatientResultUiState.Loading -> {
                    LoadingSpinner(
                        modifier = Modifier.padding(5.dp),
                        color = R.color.allergy_orange
                    )
                }

                is CreatePatientResultUiState.Success -> {
                    navigateToPatientDashboard(
                        createPatientUiState.data.id!!,
                        createPatientUiState.data.uuid!!
                    )
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
        { _, _ -> },
        { true },
        {},
        { _ -> "" },
        { _ -> "" }
    ) { _, _ -> "" }
}

@Composable
fun FullNameSection(
    name: String,
    familyName: String,
    setName: (String) -> Unit,
    setFamilyName: (String) -> Unit,
    isNameOrSurnameInvalidFormat: (String) -> Boolean
) {
    Column {
        StructureLabelText(R.string.reg_ques_name)

        TextFieldWithValidation(name, setName, isNameOrSurnameInvalidFormat, R.string.reg_firstname_hint)
        TextFieldWithValidation(familyName, setFamilyName, isNameOrSurnameInvalidFormat, R.string.reg_surname_hint)
    }
}

@Composable
fun GenderSection(gender: String, setGender: (String) -> Unit) {
    Column(Modifier.padding(vertical = 15.dp)) {
        StructureLabelText(R.string.reg_ques_gender)
        if (gender.isEmpty()) {
            Text(
                stringResource(R.string.choose_option_error),
                color = colorResource(R.color.error_red)
            )
        }
        Gender.entries.forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == it.value(),
                    onClick = { setGender(it.value()) },
                    colors = RadioButtonDefaults.colors(colorResource(R.color.allergy_orange))
                )
                Text(text = stringResource(it.relatedText()), fontSize = 16.sp, modifier = Modifier.clickable { setGender(it.value()) })
            }
        }
    }
}

@Composable
fun CountryOfBirthSection(
    countryOfBirth: String,
    setCountryOfBirth: (String) -> Unit,
    getCountryLabel: (Country) -> String
) {
    var showCountryOfBirthDialog by remember { mutableStateOf(false) }

    Column(Modifier.padding(vertical = 15.dp)) {
        StructureLabelText(R.string.country_of_birth_label)

        CountryOfBirthField(countryOfBirth) { showCountryOfBirthDialog = true }

        if (showCountryOfBirthDialog) {
            CountryOfBirthDialog(
                onCloseDialog = { showCountryOfBirthDialog = false },
                onCountrySelected = { selectedCountry ->
                    setCountryOfBirth(getCountryLabel(selectedCountry))
                }, getCountryLabel)
        }
    }
}

@Composable
fun StructureLabelText(label: Int) {
    Text(
        text = stringResource(label),
        color = Color.Gray,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 7.dp),
        fontSize = 16.sp
    )
}

@Composable
fun TextFieldWithValidation(
    name: String,
    setName: (String) -> Unit,
    isNameOrSurnameInvalidFormat: (String) -> Boolean,
    textHint: Int
) {
    var inputError by remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = name,
        onValueChange = {
            setName(it)
            inputError = isNameOrSurnameInvalidFormat(it)
        },
        label = { Text(text = stringResource(textHint)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorResource(R.color.primary),
            focusedLabelColor = colorResource(R.color.primary),
            cursorColor = Color.Black
        ),
        isError = name.isEmpty() || inputError,
        supportingText = {
            if (inputError) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.fname_invalid_error),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        singleLine = true
    )
}


@Preview
@Composable
fun CreatePatientLoadingPreview() {
    CreatePatientForm(
        { false },
        { _, _, _, _, _, _, _ -> },
        CreatePatientResultUiState.Loading,
        { _, _ -> },
        { true },
        { },
        { _ -> "" },
        { _ -> "" }
    ) { _, _ -> "" }
}

private fun checkAllFieldsFilled(name: String, familyName: String, gender: String, dateOfBirth: String, estimatedYears: String, countryOfBirth: String, legalConsentFile: String): Boolean {
    return name.isNotBlank() &&
            familyName.isNotBlank() &&
            gender.isNotBlank() &&
            (dateOfBirth.isNotBlank() || estimatedYears.isNotBlank()) &&
            countryOfBirth.isNotBlank() &&
            legalConsentFile.isNotBlank()
}

private fun isFormWithSomeInput (name: String, familyName: String, dateOfBirth: String, estimatedYears: String, countryOfBirth: String, legalConsentFile: String) : Boolean {
    return name.isNotEmpty() || familyName.isNotEmpty() || dateOfBirth.isNotEmpty() || estimatedYears.isNotEmpty() || countryOfBirth.isNotEmpty() || legalConsentFile.isNotEmpty()
}
