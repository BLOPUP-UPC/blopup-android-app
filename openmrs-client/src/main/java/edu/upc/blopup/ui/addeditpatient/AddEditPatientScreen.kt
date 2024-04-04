package edu.upc.blopup.ui.addeditpatient

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
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

    val outlinedTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colorResource(R.color.primary),
        focusedLabelColor = colorResource(R.color.primary),
        cursorColor = Color.Black,
        errorLabelColor = Color.Red,
        errorContainerColor = Color.Red)

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(Modifier.padding(vertical = 20.dp)) {
            StructureLabelText(R.string.reg_ques_name)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = { name = it },
                label = { Text(text = stringResource(R.string.reg_firstname_hint)) },
                colors = outlinedTextFieldColors
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = familyName,
                onValueChange = { familyName = it },
                label = { Text(text = stringResource(R.string.reg_surname_hint)) },
                colors = outlinedTextFieldColors
            )
        }
        Column(Modifier.padding(vertical = 20.dp)) {
            StructureLabelText(R.string.reg_ques_gender)
        }
        Column(Modifier.padding(vertical = 20.dp)) {
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
                label = { Text(text = stringResource(R.string.dob_hint),  modifier = Modifier.clickable(onClick = { showDatePickerDialog = true })) },
                colors = outlinedTextFieldColors,
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
                onValueChange = { estimatedYears = it },
                label = { Text(text = stringResource(R.string.estyr), fontSize = TextUnit(16f, TextUnitType.Sp)) },
                colors = outlinedTextFieldColors,
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
        Column(Modifier.padding(vertical = 20.dp)) {
            StructureLabelText(R.string.record_patient_consent)
        }
        SubmitButton(title = R.string.action_submit, onClickNext = { }, enabled = false )
    }
}


@Composable
fun StructureLabelText(label: Int) {
    Text(
        text = stringResource(label),
        color = Color.Gray,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 7.dp),
        fontSize = TextUnit(16f, TextUnitType.Sp)
    )
}

@Preview
@Composable
fun AddEditPatientPreview() {
    AddEditPatientForm()
}