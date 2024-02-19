package edu.upc.blopup.ui.takingvitals.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import edu.upc.R
import edu.upc.sdk.library.models.Treatment

@Composable
fun TreatmentAdherenceScreen(saveVisitAndFinishActivity: () -> Unit, treatments: List<Treatment>) {
    if (treatments.isNotEmpty()) {
        // render treatment adherence screen
        TreatmentAdherence()
        // save visit (with treatment adherence) and finish activity when user clicks on "Next"
    } else {
        saveVisitAndFinishActivity()
    }
}

@Composable
fun TreatmentAdherence() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            )
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
        Text(
            text = stringResource(R.string.treatment_adherence_text),
            fontSize = TextUnit(16f, TextUnitType.Sp),
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun AdherenceDataPreview() {
    TreatmentAdherence()
}