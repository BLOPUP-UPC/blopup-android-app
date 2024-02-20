package edu.upc.blopup.ui.takingvitals.screens

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import edu.upc.R
import edu.upc.blopup.CheckTreatment
import edu.upc.sdk.library.models.Treatment

@Composable
fun TreatmentAdherenceScreen(
    saveVisitAndFinishActivity: () -> Unit,
    treatments: List<Treatment>,
    treatmentAdherence: ((List<CheckTreatment>) -> Unit)
) {
    if (treatments.isNotEmpty()) {
        TreatmentAdherence(treatments, saveVisitAndFinishActivity, treatmentAdherence)
    } else {
        remember { saveVisitAndFinishActivity() }
    }
}

@Composable
fun TreatmentAdherence(
    treatments: List<Treatment>,
    saveVisitAndFinishActivity: () -> Unit,
    treatmentAdherence: (List<CheckTreatment>) -> Unit
) {
    val treatmentOptions = getTreatmentOptions(treatments)
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
        Column {
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
            Text(
                text = stringResource(R.string.treatment_adherence_text),
                fontSize = TextUnit(16f, TextUnitType.Sp),
            )
            TreatmentCheckBox(treatmentOptions)
        }
        OrangeButton(R.string.finalise_treatment, {
            treatmentAdherence(treatmentOptions)
            saveVisitAndFinishActivity()
        }, true)
    }
}

@Composable
fun TreatmentCheckBox(treatmentOptions: List<CheckTreatment>) {
    Column(
        Modifier.verticalScroll(rememberScrollState())
    ) {
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
                        text = checkTreatment.title,
                        fontSize = TextUnit(16f, TextUnitType.Sp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row {
                        checkTreatment.medicationType.forEachIndexed { index, medication ->
                            Text(
                                text = medication.getLabel(LocalContext.current),
                                fontSize = TextUnit(12f, TextUnitType.Sp)
                            )
                            if (index < checkTreatment.medicationType.size - 1) {
                                Text(" • ", fontSize = TextUnit(12f, TextUnitType.Sp))
                            }
                        }
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
            title = treatment.medicationName,
            medicationType = treatment.medicationType,
            selected = status,
            onCheckedChange = { status = it },
            treatmentId = treatment.treatmentUuid!!
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun TreatmentAdherencePreview() {
    TreatmentAdherence(emptyList(), {}, {})
}