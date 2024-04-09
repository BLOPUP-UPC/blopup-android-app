package edu.upc.blopup.ui.addeditpatient

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.upc.R
import java.time.Instant
import java.time.ZoneId


@Composable
fun FullNameSection(
    name: String,
    familyName: String,
    setName: (String) -> Unit,
    setFamilyName: (String) -> Unit,
    isNameOrSurnameInvalidFormat: (String) -> Boolean
) {
    var nameError by remember { mutableStateOf(false) }
    var familyNameError by remember { mutableStateOf(false) }

    Column {
        StructureLabelText(R.string.reg_ques_name)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = {
                setName(it)
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
                setFamilyName(it)
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
}

@Composable
fun GenderSection(gender: String, setGender: (String) -> Unit) {
    Column(Modifier.padding(vertical = 15.dp )) {
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
                onClick = { setGender("M") },
                colors = RadioButtonDefaults.colors(colorResource(R.color.allergy_orange))
            )
            Text(text = stringResource(R.string.male), fontSize = 16.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = gender == "F",
                onClick = { setGender("F") },
                colors = RadioButtonDefaults.colors(colorResource(R.color.allergy_orange))
            )
            Text(text = stringResource(R.string.female), fontSize = 16.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = gender == "N",
                onClick = { setGender("N") },
                colors = RadioButtonDefaults.colors(colorResource(R.color.allergy_orange))
            )
            Text(text = stringResource(R.string.non_binary), fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateOfBirthSection(
    dateOfBirth: String,
    estimatedYears: String,
    onDateOfBirth: (String) -> Unit,
    onEstimatedYears: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var formattedDateOfBirth by remember { mutableStateOf(dateOfBirth) }


    Column(Modifier.padding(vertical = 15.dp )) {
        val date = datePickerState.selectedDateMillis
        if (date != null) {
            val localDate = Instant.ofEpochMilli(date).atZone(ZoneId.of("UTC")).toLocalDate()
            formattedDateOfBirth = "${localDate.dayOfMonth}/${localDate.monthValue}/${localDate.year}"
        }
        StructureLabelText(R.string.reg_ques_dob)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = dateOfBirth,
            onValueChange = { formattedDateOfBirth = it },
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
            onValueChange = { onEstimatedYears(it) },
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
                    Button(onClick = { onDateOfBirth(formattedDateOfBirth); showDatePickerDialog = false }) {
                        Text(text = stringResource(R.string.ok))
                    }
                }) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun CountryOfBirthSection(
    countryOfBirth: String,
    setCountryOfBirth: (String) -> Unit,
    context: Context
) {
    var showCountryOfBirthDialog by remember { mutableStateOf(false) }

    Column(Modifier.padding(vertical = 15.dp )) {
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
                    setCountryOfBirth(selectedCountry.getLabel(context))
                })
        }
    }
}

@Composable
fun LegalConsentSection(context: Context, setLegalConsentFile: (String) -> Unit) {
    var showLanguagesDialog by remember { mutableStateOf(false) }
    var showLegalConsentDialog by remember { mutableStateOf(false) }
    var languageSelected by remember { mutableStateOf("") }


    Column(Modifier.padding(vertical = 15.dp )) {
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
                { setLegalConsentFile(it)})
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