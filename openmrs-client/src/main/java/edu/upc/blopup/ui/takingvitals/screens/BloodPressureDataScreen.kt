package edu.upc.blopup.ui.takingvitals.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import edu.upc.R
import edu.upc.blopup.vitalsform.Vital
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.HEART_RATE_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT

@Composable
fun BloodPressureDataScreen(
    onClickNext: () -> Unit,
    onClickBack: () -> Unit,
    vitals: MutableList<Vital>
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DataReceivedSuccessfully()
        BloodPressureDataCards(vitals)
        NavigationButtons(onClickNext, onClickBack)
    }

}

@Composable
fun BloodPressureDataCards(vitals: MutableList<Vital>) {
    Row {
        VitalsDataCard(
            modifier = Modifier.weight(0.5f),
            icon = Icons.Default.Favorite,
            contentDescription = "heart filled in black",
            title = stringResource(id = R.string.systolic_label),
            value = vitals.find { it.concept == SYSTOLIC_FIELD_CONCEPT }!!.value,
            measure = "mmHg"
        )
        VitalsDataCard(
            modifier = Modifier.weight(0.5f),
            icon = Icons.Default.FavoriteBorder,
            contentDescription = "heart outline",
            title = stringResource(id = R.string.diastolic_label),
            value = vitals.find { it.concept == DIASTOLIC_FIELD_CONCEPT }!!.value,
            measure = "mmHg"
        )
        VitalsDataCard(
            modifier = Modifier.weight(0.5f),
            icon = ImageVector.vectorResource(id = R.drawable.pulse_icon),
            contentDescription = "pulse symbol",
            title = stringResource(id = R.string.pulse_label),
            value = vitals.find { it.concept == HEART_RATE_FIELD_CONCEPT }!!.value,
            measure = "/min"
        )
    }
}


//@Preview
//@Composable
//fun BloodPressureDataScreenPreview(@PreviewParameter(DataScreenParameters::class,1) vitals: MutableList<Vital>) {
//    BloodPressureDataScreen(
//        {},
//        {},
//        vitals,
//        { viewModel.showDialogState }) { viewModel.setShowDialogState(false) }
//}
