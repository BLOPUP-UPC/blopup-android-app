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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import edu.upc.R
import edu.upc.blopup.CheckTreatment
import edu.upc.blopup.ui.takingvitals.ResultUiState
import edu.upc.sdk.library.models.Treatment

@Composable
fun TreatmentAdherenceScreen(
    setResultAndFinish: (Int) -> Unit,
    createVisit: () -> Unit,
    createVisitResultUiState: ResultUiState?,
    treatmentsResultUiState: ResultUiState,
    treatmentAdherence: (List<CheckTreatment>) -> Unit,
    getActiveTreatments: () -> Unit
) {
    when (createVisitResultUiState) {
        ResultUiState.Loading -> {
            LoadingSpinner()
        }

        ResultUiState.Error -> {
            ErrorDialog(show = createVisitResultUiState is ResultUiState.Error,
                onDismiss = { setResultAndFinish(Activity.RESULT_CANCELED) },
                onConfirm = { createVisit() })
        }

        is ResultUiState.Success<*> -> {
            setResultAndFinish(Activity.RESULT_OK)
        }

        else -> {}
    }


    if (treatmentsResultUiState is ResultUiState.Success<*> && (treatmentsResultUiState.data as List<*>).isEmpty()) {
        LaunchedEffect(true) {
            createVisit()
        }
    } else {
        TreatmentAdherence(treatmentsResultUiState, createVisit, treatmentAdherence, getActiveTreatments)
    }
}

@Composable
fun TreatmentAdherence(
    treatments: ResultUiState,
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
                    fontSize = TextUnit(20f, TextUnitType.Sp)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            when (treatments) {
                ResultUiState.Loading -> {
                    LoadingSpinner()
                }

                is ResultUiState.Success<*> -> {
                    Text(
                        text = stringResource(R.string.treatment_adherence_text),
                        fontSize = TextUnit(16f, TextUnitType.Sp),
                    )
                    treatmentOptions = getTreatmentOptions(treatments.data as List<*>)
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
        OrangeButton(R.string.finalise_treatment, {
            if (treatments is ResultUiState.Success<*>) {
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
                        fontSize = TextUnit(16f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        val medicationTypesString = checkTreatment.medicationType
                            .map { it.getLabel(LocalContext.current) }
                            .joinToString(" • ")
                        Text(
                            text = medicationTypesString,
                            fontSize = TextUnit(14f, TextUnitType.Sp),
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun getTreatmentOptions(treatments: List<*>): List<CheckTreatment> {
    return treatments.map { item ->
        val treatment = item as Treatment
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
    TreatmentAdherence(ResultUiState.Loading, {}, {}, {} )
}

@Preview(showSystemUi = true)
@Composable
fun TreatmentAdherencePreviewSuccess() {
    TreatmentAdherence(
        ResultUiState.Success<List<Treatment>>(emptyList()),
        {},
        {},
        {}
    )
}