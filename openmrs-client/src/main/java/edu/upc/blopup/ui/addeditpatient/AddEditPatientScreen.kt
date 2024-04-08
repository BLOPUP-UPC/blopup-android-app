package edu.upc.blopup.ui.addeditpatient

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import edu.upc.R
import edu.upc.blopup.ui.shared.components.SubmitButton
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientViewModel
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardActivity
import edu.upc.sdk.utilities.ApplicationConstants
import java.time.Instant
import java.time.ZoneId

@Composable
fun AddEditPatientScreen(viewModel: AddEditPatientViewModel = hiltViewModel()) {
    AddEditPatientForm(
        viewModel::isNameOrSurnameInvalidFormat,
        viewModel::setPatientData,
        viewModel::confirmPatient
    )
    }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPatientForm(
    isNameOrSurnameInvalidFormat: (String) -> Boolean,
    setPatientData: (String, String, String, String, String, String) -> Unit,
    updateOrRegisterPatient: () -> Unit,
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var familyName by remember { mutableStateOf("") }
    var familyNameError by remember { mutableStateOf(false) }
    var gender by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var estimatedYears by remember { mutableStateOf("") }
    var countryOfBirth by remember { mutableStateOf("") }
    var languageSelected by remember { mutableStateOf("") }
    var legalConsentFile by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState()
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showCountryOfBirthDialog by remember { mutableStateOf(false) }
    var showLanguagesDialog by remember { mutableStateOf(false) }
    var showLegalConsentDialog by remember { mutableStateOf(false) }

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
        Column {
            StructureLabelText(R.string.reg_ques_name)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = {
                    name = it
                    nameError = isNameOrSurnameInvalidFormat(it)
                },
                label = { Text(text = stringResource(R.string.reg_firstname_hint)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.primary),
                    focusedLabelColor = colorResource(R.color.primary),
                    cursorColor = Color.Black
                ),
                isError = name.isEmpty() || nameError,
                supportingText = {
                    if (nameError) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.fname_invalid_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = familyName,
                onValueChange = {
                    familyName = it
                    familyNameError = isNameOrSurnameInvalidFormat(it)
                },
                label = { Text(text = stringResource(R.string.reg_surname_hint)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.primary),
                    focusedLabelColor = colorResource(R.color.primary),
                    cursorColor = Color.Black
                ),
                isError = familyName.isEmpty() || familyNameError,
                supportingText = {
                    if (familyNameError) {
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
        Column {
            StructureLabelText(R.string.reg_ques_gender)
            if (gender.isEmpty()) {
                Text(
                    stringResource(R.string.choose_option_error),
                    color = colorResource(R.color.error_red)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == "M",
                    onClick = { gender = "M" },
                    colors = RadioButtonDefaults.colors(colorResource(R.color.allergy_orange))
                )
                Text(text = stringResource(R.string.male), fontSize = 16.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == "F",
                    onClick = { gender = "F" },
                    colors = RadioButtonDefaults.colors(colorResource(R.color.allergy_orange))
                )
                Text(text = stringResource(R.string.female), fontSize = 16.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == "N",
                    onClick = { gender = "N" },
                    colors = RadioButtonDefaults.colors(colorResource(R.color.allergy_orange))
                )
                Text(text = stringResource(R.string.non_binary), fontSize = 16.sp)
            }
        }
        Column {
            val date = datePickerState.selectedDateMillis
            if (date != null) {
                val localDate = Instant.ofEpochMilli(date).atZone(ZoneId.of("UTC")).toLocalDate()
                dateOfBirth = "${localDate.dayOfMonth}/${localDate.monthValue}/${localDate.year}"
            }
            StructureLabelText(R.string.reg_ques_dob)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                isError = dateOfBirth.isEmpty() && estimatedYears.isEmpty(),
                label = {
                    Text(
                        text = stringResource(R.string.dob_hint),
                        modifier = Modifier.clickable(onClick = { showDatePickerDialog = true })
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.primary),
                    focusedLabelColor = colorResource(R.color.primary),
                    cursorColor = Color.Black
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.clickable(onClick = { showDatePickerDialog = true })
                    )
                })
            Text(
                text = stringResource(id = R.string.label_or),
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = estimatedYears,
                isError = dateOfBirth.isEmpty() && estimatedYears.isEmpty(),
                onValueChange = { estimatedYears = it },
                label = { Text(text = stringResource(R.string.estyr), fontSize = 16.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.primary),
                    focusedLabelColor = colorResource(R.color.primary),
                    cursorColor = Color.Black
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            if (showDatePickerDialog) {
                DatePickerDialog(onDismissRequest = { showDatePickerDialog = false },
                    confirmButton = {
                        Button(onClick = { showDatePickerDialog = false }) {
                            Text(text = stringResource(R.string.ok))
                        }
                    }) {
                    DatePicker(state = datePickerState)
                }
            }
        }
        Column {
            StructureLabelText(R.string.country_of_birth_label)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCountryOfBirthDialog = true }
                    .border(
                        width = 1.dp,
                        color = if (countryOfBirth.isEmpty()) MaterialTheme.colorScheme.error else Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 15.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = countryOfBirth.ifEmpty { stringResource(R.string.country_of_birth_default) },
                        fontSize = 16.sp,
                        color = if (countryOfBirth.isEmpty()) MaterialTheme.colorScheme.error else Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        tint = Color.Gray,
                        contentDescription = null
                    )
                }
            }
            if (showCountryOfBirthDialog) {
                CountryOfBirthDialog(
                    onCloseDialog = { showCountryOfBirthDialog = false },
                    onCountrySelected = { selectedCountry ->
                        countryOfBirth = selectedCountry.getLabel(context)
                    })
            }
        }
        Column {
            StructureLabelText(R.string.record_patient_consent)
            Box(
                modifier = Modifier
                    .clickable { showLanguagesDialog = true }
                    .border(
                        width = 1.dp,
                        color = if (languageSelected.isEmpty()) MaterialTheme.colorScheme.error else Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 15.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = languageSelected.ifEmpty { stringResource(R.string.select_language) },
                        fontSize = 16.sp,
                        color = if (languageSelected.isEmpty()) MaterialTheme.colorScheme.error else Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        tint = Color.Gray,
                        contentDescription = null
                    )
                }
                if (showLanguagesDialog) {
                    LanguagesDialog(context, { language ->
                        languageSelected = language
                    }) { showLanguagesDialog = false }
                }
            }
            Text(
                text = stringResource(id = R.string.record_legal_consent_u),
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .clickable {
                        if (languageSelected.isNotEmpty()) {
                            showLegalConsentDialog = true
                        }
                    },
                color = if (languageSelected.isEmpty()) Color.Gray else colorResource(R.color.allergy_orange)
            )
            if (showLegalConsentDialog) {
                LegalConsentDialog(
                    languageSelected,
                    { showLegalConsentDialog = false },
                    context,
                    { legalConsentFile = it })
            }
        }
        Column {
            SubmitButton(
                title = R.string.action_submit,
                onClickNext = {
                    setPatientData(
                        name,
                        familyName,
                        dateOfBirth,
                        estimatedYears,
                        gender,
                        countryOfBirth
                    )
                    updateOrRegisterPatient()
                },
                enabled = isSubmitEnabled
            )
        }
    }
}

private fun startPatientDashboardActivity(context: Context, patientId: Long?) {
    Intent(context, PatientDashboardActivity::class.java).apply {
        putExtra(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE, patientId)
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

@Preview
@Composable
fun AddEditPatientPreview() {
    AddEditPatientForm({ false }, { _, _, _, _, _, _ -> }, {})
}

@Preview
@Composable
fun LanguageDialogPreview() {
    LanguagesDialog(LocalContext.current, {}) {}
}