package edu.upc.blopup.ui.takingvitals.screens

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
import edu.upc.blopup.ui.ResultUiState
import edu.upc.blopup.ui.takingvitals.components.DataReceivedSuccessfully
import edu.upc.blopup.ui.takingvitals.components.DataScreenParameters
import edu.upc.blopup.ui.takingvitals.components.ErrorDialog
import edu.upc.blopup.ui.takingvitals.components.LoadingSpinner
import edu.upc.blopup.ui.takingvitals.components.NavigationButtons
import edu.upc.blopup.ui.takingvitals.components.OnBackPressButtonConfirmDialog
import edu.upc.blopup.ui.takingvitals.components.VitalDataCard
import edu.upc.sdk.library.models.Vital
import edu.upc.sdk.utilities.ApplicationConstants.VitalsConceptType.WEIGHT_FIELD_CONCEPT

@Composable
fun WeightDataScreen(
    onClickNext: () -> Unit,
    onClickBack: () -> Unit,
    vitals: MutableList<Vital>,
    scaleBluetoothConnectionResultUiState: ResultUiState,
    receiveWeightData: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (scaleBluetoothConnectionResultUiState) {
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
                WeighDataCard(vitals)
                NavigationButtons(onClickNext, onClickBack)
                OnBackPressButtonConfirmDialog(onClickBack)
            }

            is ResultUiState.Error -> {
                ErrorDialog(
                    show = scaleBluetoothConnectionResultUiState is ResultUiState.Error,
                    onDismiss = { onClickBack() },
                    onConfirm = { receiveWeightData() },
                    title = R.string.bluetooth_error_connection,
                    instructions = R.string.bluetooth_error_instructions_scale
                )
            }
        }
    }
}


@Composable
fun WeighDataCard(vitals: MutableList<Vital>) {
    VitalDataCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        icon = ImageVector.vectorResource(id = R.drawable.scale_icon),
        contentDescription = "Scale icon",
        title = stringResource(id = R.string.weight_value_label),
        value = vitals.find { it.concept == WEIGHT_FIELD_CONCEPT }?.value ?: "--",
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
}