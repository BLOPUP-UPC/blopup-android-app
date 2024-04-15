package edu.upc.blopup.ui.addeditpatient

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
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
    Column {
        StructureLabelText(R.string.reg_ques_name)

        TextFieldWithValidation(name, setName, isNameOrSurnameInvalidFormat, R.string.reg_firstname_hint)
        TextFieldWithValidation(familyName, setFamilyName, isNameOrSurnameInvalidFormat, R.string.reg_surname_hint)
    }
}

@Composable
fun GenderSection(gender: String, setGender: (String) -> Unit) {
    val genderOptions = listOf(
        Pair("M", R.string.male),
        Pair("F", R.string.female),
        Pair("N", R.string.non_binary)
    )

    Column(Modifier.padding(vertical = 15.dp)) {
        StructureLabelText(R.string.reg_ques_gender)
        if (gender.isEmpty()) {
            Text(
                stringResource(R.string.choose_option_error),
                color = colorResource(R.color.error_red)
            )
        }
        genderOptions.forEach { (value, labelResource) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == value,
                    onClick = { setGender(value) },
                    colors = RadioButtonDefaults.colors(colorResource(R.color.allergy_orange))
                )
                Text(text = stringResource(labelResource), fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateOfBirthSection(
    dateOfBirth: String,
    estimatedYears: String,
    onDateOfBirth: (String) -> Unit,
    onEstimatedYears: (String) -> Unit,
    isBirthDateValidRange: (String) -> Boolean
) {
    val datePickerState = rememberDatePickerState()
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var formattedDateOfBirth by remember { mutableStateOf(dateOfBirth) }
    var birthdateError by remember { mutableStateOf(false) }


    Column(Modifier.padding(vertical = 15.dp)) {
        val date = datePickerState.selectedDateMillis
        if (date != null) {
            val localDate = Instant.ofEpochMilli(date).atZone(ZoneId.of("UTC")).toLocalDate()
            formattedDateOfBirth =
                "${localDate.dayOfMonth}/${localDate.monthValue}/${localDate.year}"
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
            isError = (dateOfBirth.isEmpty() && estimatedYears.isEmpty()) || birthdateError,
            onValueChange = {
                onEstimatedYears(it)
                birthdateError = isBirthDateValidRange(it)
                onDateOfBirth("")
            },
            label = { Text(text = stringResource(R.string.estyr), fontSize = 16.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.primary),
                focusedLabelColor = colorResource(R.color.primary),
                cursorColor = Color.Black
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            supportingText = {
                if (birthdateError) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.age_out_of_bounds_message),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
        )
        if (showDatePickerDialog) {
            DatePickerDialog(onDismissRequest = { showDatePickerDialog = false },
                confirmButton = {
                    Button(onClick = {
                        onDateOfBirth(formattedDateOfBirth); showDatePickerDialog = false; onEstimatedYears("")
                    }) {
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

    Column(Modifier.padding(vertical = 15.dp)) {
        StructureLabelText(R.string.country_of_birth_label)

        CountryOfBirthField(countryOfBirth) { showCountryOfBirthDialog = true }

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
fun LegalConsentSection(
    setLegalConsentFile: (String) -> Unit,
    legalConsentFile: String,
    getStringByResourceId: (Int) -> String,
    ) {
    var showLanguagesDropDownList by remember { mutableStateOf(false) }
    var showLegalConsentDialog by remember { mutableStateOf(false) }
    var languageSelected by remember { mutableStateOf(getStringByResourceId(R.string.select_language)) }


    Column(Modifier.padding(vertical = 15.dp)) {
        StructureLabelText(R.string.record_patient_consent)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clickable { showLanguagesDropDownList = true }
                    .border(
                        width = 1.dp,
                        color = if (languageSelected == getStringByResourceId(R.string.select_language)) MaterialTheme.colorScheme.error else Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(15.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 15.dp)
                            .width(145.dp)
                    ) {
                        Text(
                            text = languageSelected,
                            fontSize = 16.sp,
                            color = if (languageSelected == getStringByResourceId(R.string.select_language)) MaterialTheme.colorScheme.error else Color.Black,
                        )
                    }
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        tint = Color.Gray,
                        contentDescription = null,
                    )
                }
                ShowLanguagesDropDownList(
                    showLanguagesDropDownList,
                    { showLanguagesDropDownList = false },
                    { language -> languageSelected = language })
            }
            if (legalConsentFile.isNotEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.saved_recording_icon),
                    contentDescription = "Saved recording icon",
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .size(35.dp),
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.allergy_orange)),
                )
            }

        }

        Text(
            text = if (legalConsentFile.isEmpty()) stringResource(id = R.string.record_legal_consent_u) else stringResource(
                id = R.string.record_again_legal_consent
            ),
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .clickable {
                    if (languageSelected != getStringByResourceId(R.string.select_language)) {
                        showLegalConsentDialog = true
                    }
                },
            color = if (languageSelected == getStringByResourceId(R.string.select_language)) Color.Gray else colorResource(
                R.color.allergy_orange
            )
        )
        if (showLegalConsentDialog) {
            LegalConsentDialog(
                languageSelected,
                { showLegalConsentDialog = false },
                { setLegalConsentFile(it) },
                legalConsentFile
            )
        }
    }
}

@Composable
fun ShowLanguagesDropDownList(
    showLanguagesDialog: Boolean,
    closeDropDown: () -> Unit,
    onLanguageSelected: (String) -> Unit,
) {
    val context = LocalContext.current
    val languagesList = context.resources.getStringArray(R.array.languages)
    DropdownMenu(
        expanded = showLanguagesDialog,
        onDismissRequest = { closeDropDown() },
        modifier = Modifier
            .background(colorResource(R.color.white))
            .padding(5.dp)
    ) {
        languagesList.forEach { language ->
            DropdownMenuItem(
                onClick = {
                    onLanguageSelected(language)
                    closeDropDown()
                },
                text = { Text(text = language, color = Color.Gray) })
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
    )}

@Composable
fun CountryOfBirthField(countryOfBirth: String, onShowCountryOfBirthDialog: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onShowCountryOfBirthDialog() }
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
    }}