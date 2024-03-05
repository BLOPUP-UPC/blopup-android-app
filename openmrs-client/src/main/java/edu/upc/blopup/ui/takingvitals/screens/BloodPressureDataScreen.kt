package edu.upc.blopup.ui.takingvitals.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import edu.upc.blopup.ui.takingvitals.ResultUiState
import edu.upc.blopup.ui.takingvitals.Vital
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.HEART_RATE_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT

@Composable
fun BloodPressureDataScreen(
    onClickNext: () -> Unit,
    onClickBack: () -> Unit,
    vitals: MutableList<Vital>,
    bpBluetoothConnectionResultUiState: ResultUiState,
    receiveBpData: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (bpBluetoothConnectionResultUiState) {
            is ResultUiState.Loading -> {
                LoadingSpinner(
                    Modifier
                        .width(70.dp)
                        .padding(top = 200.dp)
                )
                Text(
                    text = stringResource(R.string.waiting_for_data),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(70.dp)
                )
            }

            is ResultUiState.Success<*> -> {
                DataReceivedSuccessfully()
                BloodPressureDataCards(vitals)
                NavigationButtons(onClickNext, onClickBack)
                OnBackPressButtonConfirmDialog(onClickBack)
            }

            is ResultUiState.Error -> {
                ErrorDialog(
                    show = bpBluetoothConnectionResultUiState is ResultUiState.Error,
                    onDismiss = { onClickBack() },
                    onConfirm = { receiveBpData() },
                    title = R.string.bluetooth_error_connection,
                    instructions = R.string.bluetooth_error_instructions_bp
                )
            }
        }
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
            value = vitals.find { it.concept == SYSTOLIC_FIELD_CONCEPT }?.value ?: "--",
            measure = "mmHg"
        )
        VitalsDataCard(
            modifier = Modifier.weight(0.5f),
            icon = Icons.Default.FavoriteBorder,
            contentDescription = "heart outline",
            title = stringResource(id = R.string.diastolic_label),
            value = vitals.find { it.concept == DIASTOLIC_FIELD_CONCEPT }?.value ?: "--",
            measure = "mmHg"
        )
        VitalsDataCard(
            modifier = Modifier.weight(0.5f),
            icon = ImageVector.vectorResource(id = R.drawable.pulse_icon),
            contentDescription = "pulse symbol",
            title = stringResource(id = R.string.pulse_label),
            value = vitals.find { it.concept == HEART_RATE_FIELD_CONCEPT }?.value ?: "--",
            measure = "/min"
        )
    }
}


@Preview
@Composable
fun BloodPressureDataScreenPreviewSuccess(
    @PreviewParameter(
        DataScreenParameters::class,
        1
    ) vitals: MutableList<Vital>
) {
    BloodPressureDataScreen(
        {},
        {},
        vitals,
        ResultUiState.Success(Unit),
        {}
    )
}

@Preview
@Composable
fun BloodPressureDataScreenPreviewError(
    @PreviewParameter(
        DataScreenParameters::class,
        1
    ) vitals: MutableList<Vital>
) {
    BloodPressureDataScreen(
        {},
        {},
        vitals,
        ResultUiState.Error,
        {}
    )
}

@Preview
@Composable
fun BloodPressureDataScreenPreviewLoading(
    @PreviewParameter(
        DataScreenParameters::class,
        1
    ) vitals: MutableList<Vital>
) {
    BloodPressureDataScreen(
        {},
        {},
        vitals,
        ResultUiState.Loading,
        {}
    )
}

