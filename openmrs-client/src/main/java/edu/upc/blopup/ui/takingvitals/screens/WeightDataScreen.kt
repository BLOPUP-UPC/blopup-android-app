package edu.upc.blopup.ui.takingvitals.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import edu.upc.R
import edu.upc.blopup.vitalsform.Vital
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT

@Composable
fun WeightDataScreen(onClickNext: () -> Unit, onClickBack: () -> Unit, vitals: MutableList<Vital>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (vitals.find { it.concept == WEIGHT_FIELD_CONCEPT } != null) {
            Log.i("WeightDataScreen", "Weight data: $vitals")
            DataReceivedSuccessfully()
            WeighDataCard(vitals)
            NavigationButtons(onClickNext, onClickBack)
            OnBackPressButtonConfirmDialog(onClickBack)
        } else {
                LoadingSpinner(Modifier
                    .width(70.dp)
                    .padding(top = 200.dp))
                Text(
                    text = stringResource(R.string.waiting_for_data),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(70.dp)
                )
        }
    }
}


@Composable
fun WeighDataCard(vitals: MutableList<Vital>) {
    VitalsDataCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        icon = ImageVector.vectorResource(id = R.drawable.scale_icon),
        contentDescription = "Scale icon",
        title = stringResource(id = R.string.weight_value_label),
        value = vitals.find { it.concept == WEIGHT_FIELD_CONCEPT }!!.value,
        measure = "Kg"
    )
}

@Preview
@Composable
fun WeightDataScreenPreview(
    @PreviewParameter(
        DataScreenParameters::class,
        1
    ) vitals: MutableList<Vital>
) {
    Column {
        LoadingSpinner(Modifier
            .width(70.dp)
            .padding(top = 200.dp))
        Text(
            text = stringResource(R.string.waiting_for_data),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(70.dp)
        )
    }
}