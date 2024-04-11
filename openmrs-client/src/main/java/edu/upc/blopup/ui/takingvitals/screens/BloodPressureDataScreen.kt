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
import androidx.compose.ui.unit.dp
import edu.upc.R
import edu.upc.blopup.model.BloodPressure
import edu.upc.blopup.ui.ResultUiState
import edu.upc.blopup.ui.shared.components.ErrorDialog
import edu.upc.blopup.ui.shared.components.LoadingSpinner
import edu.upc.blopup.ui.takingvitals.components.DataReceivedSuccessfully
import edu.upc.blopup.ui.takingvitals.components.NavigationButtons
import edu.upc.blopup.ui.takingvitals.components.VitalDataCard

@Composable
fun BloodPressureDataScreen(
    onClickNext: () -> Unit,
    onClickBack: () -> Unit,
    bloodPressureState: ResultUiState<BloodPressure>,
    receiveBpData: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (bloodPressureState) {
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

            is ResultUiState.Success -> {
                DataReceivedSuccessfully()
                BloodPressureDataCards(bloodPressureState.data)
                NavigationButtons(onClickNext, onClickBack)
            }

            is ResultUiState.Error -> {
                ErrorDialog(
                    show = true,
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
fun BloodPressureDataCards(bloodPressureState: BloodPressure?) {
    Row {
        VitalDataCard(
            modifier = Modifier.weight(0.5f),
            icon = Icons.Default.Favorite,
            contentDescription = "heart filled in black",
            title = stringResource(id = R.string.systolic_label),
            value = bloodPressureState?.systolic.toString(),
            measure = "mmHg"
        )
        VitalDataCard(
            modifier = Modifier.weight(0.5f),
            icon = Icons.Default.FavoriteBorder,
            contentDescription = "heart outline",
            title = stringResource(id = R.string.diastolic_label),
            value = bloodPressureState?.diastolic.toString(),
            measure = "mmHg"
        )
        VitalDataCard(
            modifier = Modifier.weight(0.5f),
            icon = ImageVector.vectorResource(id = R.drawable.pulse_icon),
            contentDescription = "pulse symbol",
            title = stringResource(id = R.string.pulse_label),
            value = bloodPressureState?.pulse.toString(),
            measure = "/min"
        )
    }
}


@Preview
@Composable
fun BloodPressureDataScreenPreviewSuccess() {
    BloodPressureDataScreen(
        {},
        {},
        ResultUiState.Success(BloodPressure(122, 80, 60))
    ) {}
}

@Preview
@Composable
fun BloodPressureDataScreenPreviewError() {
    BloodPressureDataScreen(
        {},
        {},
        ResultUiState.Error
    ) {}
}

@Preview
@Composable
fun BloodPressureDataScreenPreviewLoading() {
    BloodPressureDataScreen(
        {},
        {},
        ResultUiState.Loading
    ) {}
}

