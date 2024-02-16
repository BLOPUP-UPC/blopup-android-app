package edu.upc.blopup.ui.takingvitals.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Treatment Adherence Screen")
    }
}