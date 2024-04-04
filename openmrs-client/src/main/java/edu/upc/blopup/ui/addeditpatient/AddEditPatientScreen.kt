package edu.upc.blopup.ui.addeditpatient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.upc.R
import edu.upc.blopup.ui.shared.components.SubmitButton
import java.time.Instant
import java.time.ZoneId

@Composable
fun AddEditPatientScreen(viewModel: AddEditPatientViewModel = hiltViewModel()) {
    AddEditPatientForm()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPatientForm() {
    var name by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState()
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var estimatedYears by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

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
                onValueChange = { name = it },
                label = { Text(text = stringResource(R.string.reg_firstname_hint)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.primary),
                    focusedLabelColor = colorResource(R.color.primary),
                    cursorColor = Color.Black),
                isError = name.isEmpty()
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = familyName,
                onValueChange = { familyName = it },
                label = { Text(text = stringResource(R.string.reg_surname_hint)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.primary),
                    focusedLabelColor = colorResource(R.color.primary),
                    cursorColor = Color.Black),
                isError = familyName.isEmpty()

            )
        }
        Column {
            StructureLabelText(R.string.reg_ques_gender)
            if(gender.isEmpty()) {
                Text(
                    stringResource(R.string.choose_option_error),
                    color = colorResource(R.color.error_red)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = gender == "M" , onClick = { gender = "M"}, colors = RadioButtonDefaults.colors(colorResource(R.color.allergy_orange)))
                Text(text = stringResource(R.string.male), fontSize = 16.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = gender == "F", onClick = { gender = "F"}, colors = RadioButtonDefaults.colors(colorResource(R.color.allergy_orange)))
                Text(text = stringResource(R.string.female), fontSize = 16.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = gender == "N", onClick = { gender = "N"}, colors = RadioButtonDefaults.colors(colorResource(R.color.allergy_orange)))
                Text(text = stringResource(R.string.non_binary), fontSize = 16.sp)
            }
        }
        Column {
            val date = datePickerState.selectedDateMillis
            if(date != null) {
                val localDate = Instant.ofEpochMilli(date).atZone(ZoneId.of("UTC")).toLocalDate()
                dateOfBirth = "${localDate.dayOfMonth}/${localDate.monthValue}/${localDate.year}"
            }
            StructureLabelText(R.string.reg_ques_dob)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                isError = dateOfBirth.isEmpty() && estimatedYears.isEmpty(),
                label = { Text(text = stringResource(R.string.dob_hint),  modifier = Modifier.clickable(onClick = { showDatePickerDialog = true })) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.primary),
                    focusedLabelColor = colorResource(R.color.primary),
                    cursorColor = Color.Black),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.clickable(onClick = { showDatePickerDialog = true })
                    )
                })
            Text(text = stringResource(id = R.string.label_or), textAlign = TextAlign.Center, color = Color.Gray, modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp), fontWeight = FontWeight.Bold)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = estimatedYears,
                isError = dateOfBirth.isEmpty() && estimatedYears.isEmpty(),
                onValueChange = { estimatedYears = it },
                label = { Text(text = stringResource(R.string.estyr), fontSize = 16.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.primary),
                    focusedLabelColor = colorResource(R.color.primary),
                    cursorColor = Color.Black),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            if (showDatePickerDialog) {
                DatePickerDialog(onDismissRequest = { showDatePickerDialog = false },
                    confirmButton = {
                        Button(onClick = {showDatePickerDialog = false }) {
                            Text(text = stringResource(R.string.ok))
                        }
                    }) {
                    DatePicker(state = datePickerState)
                }
            }
        }
        Column {
            StructureLabelText(R.string.record_patient_consent)
        }
        Column {
            SubmitButton(title = R.string.action_submit, onClickNext = { }, enabled = false )
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
    AddEditPatientForm()
}