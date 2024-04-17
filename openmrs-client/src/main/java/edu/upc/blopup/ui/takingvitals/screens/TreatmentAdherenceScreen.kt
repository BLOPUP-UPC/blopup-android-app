package edu.upc.blopup.ui.takingvitals.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.upc.R
import edu.upc.blopup.CheckTreatment
import edu.upc.blopup.model.Doctor
import edu.upc.blopup.model.MedicationType
import edu.upc.blopup.model.Treatment
import edu.upc.blopup.ui.ResultUiState
import edu.upc.blopup.ui.shared.components.ErrorDialog
import edu.upc.blopup.ui.shared.components.LoadingSpinner
import edu.upc.blopup.ui.shared.components.SubmitButton

@Composable
fun TreatmentAdherenceScreen(
    setResultAndFinish: (Int, String?) -> Unit,
    createVisit: () -> Unit,
    createVisitResultUiState: CreateVisitResultUiState,
    treatmentsResultUiState: ResultUiState<List<Treatment>>,
    treatmentAdherence: (List<CheckTreatment>) -> Unit,
    getActiveTreatments: () -> Unit
) {
    when (createVisitResultUiState) {
        CreateVisitResultUiState.Loading -> {
            LoadingSpinner(
                Modifier
                    .width(70.dp)
                    .padding(top = 200.dp)
            )
        }

        CreateVisitResultUiState.Error -> {
            ErrorDialog(
                show = true,
                onDismiss = { setResultAndFinish(Activity.RESULT_CANCELED, null) },
                onConfirm = { createVisit() },
                title = R.string.visit_start_error
            )
        }

        is CreateVisitResultUiState.Success -> {
            setResultAndFinish(Activity.RESULT_OK, createVisitResultUiState.visit.id.toString())
        }

        CreateVisitResultUiState.NotStarted -> {}
    }


    if (treatmentsResultUiState is ResultUiState.Success && treatmentsResultUiState.data.isEmpty()) {
        LaunchedEffect(true) {
            createVisit()
        }
    } else {
        TreatmentAdherence(
            treatmentsResultUiState,
            createVisit,
            treatmentAdherence,
            getActiveTreatments
        )
    }
}

@Composable
fun TreatmentAdherence(
    treatments: ResultUiState<List<Treatment>>,
    createVisit: () -> Unit,
    treatmentAdherence: (List<CheckTreatment>) -> Unit,
    getActiveTreatments: () -> Unit
) {
    var treatmentOptions = emptyList<CheckTreatment>()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
        verticalArrangement = Arrangement.SpaceBetween

    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(weight = 1f, fill = false)
                .padding(bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.treatment_adherence_img),
                contentDescription = stringResource(R.string.adherence_description),
                modifier = Modifier
                    .padding(30.dp)
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            Text(
                text = stringResource(R.string.treatment_adherence_title),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            when (treatments) {
                ResultUiState.Loading -> {
                    LoadingSpinner(
                        Modifier
                            .width(50.dp)
                            .padding(top = 50.dp)
                    )
                }

                is ResultUiState.Success -> {
                    Text(
                        text = stringResource(R.string.treatment_adherence_text),
                        fontSize = 16.sp,
                    )
                    treatmentOptions = getTreatmentOptions(treatments.data)
                    TreatmentCheckBox(treatmentOptions)
                }

                ResultUiState.Error -> {
                    Text(
                        text = stringResource(R.string.error_loading_treatments),
                        color = colorResource(R.color.allergy_orange)
                    )
                    Text(
                        text = stringResource(R.string.error_loading_treatments_try_again),
                        color = colorResource(R.color.allergy_orange),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { getActiveTreatments() }
                    )
                }
            }
        }
        SubmitButton(R.string.finalise_treatment, {
            if (treatments is ResultUiState.Success) {
                treatmentAdherence(treatmentOptions)
            }
            createVisit()
        }, true)
    }
}

@Composable
fun TreatmentCheckBox(treatmentOptions: List<CheckTreatment>) {
    Column {
        treatmentOptions.forEach { checkTreatment ->
            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = checkTreatment.selected,
                    onCheckedChange = { checkTreatment.onCheckedChange(!checkTreatment.selected) },
                    colors = CheckboxDefaults.colors(checkedColor = Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = checkTreatment.medicationName,
                        modifier = Modifier.padding(bottom = 4.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        val medicationTypesString = checkTreatment.medicationType
                            .map { it.getLabel(LocalContext.current) }
                            .joinToString(" • ")
                        Text(
                            text = medicationTypesString,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun getTreatmentOptions(treatments: List<Treatment>): List<CheckTreatment> {
    return treatments.map { treatment ->
        var status by rememberSaveable { mutableStateOf(false) }
        CheckTreatment(
            medicationName = treatment.medicationName,
            medicationType = treatment.medicationType,
            selected = status,
            onCheckedChange = { status = it },
            treatmentId = treatment.treatmentUuid!!
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun TreatmentAdherencePreviewError() {
    TreatmentAdherence(ResultUiState.Error, {}, {}, {})
}

@Preview(showSystemUi = true)
@Composable
fun TreatmentAdherencePreviewLoading() {
    TreatmentAdherence(ResultUiState.Loading, {}, {}, {})
}

@Preview(showSystemUi = true)
@Composable
fun TreatmentAdherencePreviewSuccess() {
    TreatmentAdherence(
        ResultUiState.Success(
            listOf(
                Treatment(
                    "uuid",
                    "Medication Name",
                    setOf(MedicationType.DIURETIC),
                    treatmentUuid = "uuidTreatment",
                )
            )
        ),
        {},
        {},
        {}
    )
}