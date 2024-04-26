package edu.upc.blopup.ui.createpatient.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.upc.R
import edu.upc.blopup.ui.createpatient.StructureLabelText
import edu.upc.sdk.utilities.DateUtils.formatToDefaultFormat
import edu.upc.sdk.utilities.DateUtils.toLocalDate
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateOfBirthSection(
    dateOfBirth: String,
    estimatedYears: String,
    setDateOfBirth: (String) -> Unit,
    setEstimatedYears: (String) -> Unit,
    isBirthDateValidRange: (String) -> Boolean
) {
    val datePickerState = rememberDatePickerState()
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var formattedDateOfBirth by remember { mutableStateOf(dateOfBirth) }
    var birthdateError by remember { mutableStateOf(false) }


    Column(Modifier.padding(vertical = 15.dp)) {
        val date = datePickerState.selectedDateMillis
        if (date != null) {
            formattedDateOfBirth = Instant.ofEpochMilli(date).toLocalDate().formatToDefaultFormat()
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
                setEstimatedYears(it)
                birthdateError = isBirthDateValidRange(it)
                setDateOfBirth("")
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
                        setDateOfBirth(formattedDateOfBirth); showDatePickerDialog = false; setEstimatedYears("")
                    }) {
                        Text(text = stringResource(R.string.ok))
                    }
                }) {
                DatePicker(state = datePickerState)
            }
        }
    }
}