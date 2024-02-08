package edu.upc.blopup.vitalsform.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import edu.upc.R


@Composable
fun HowToActivateBPDeviceScreen(paddingValues: PaddingValues, navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
            ),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        BloodPressureDeviceInstructions()
        ReceiveBloodPressureDataButton(navController)
    }
}

@Composable
fun BloodPressureDeviceInstructions() {
    Column {
        Image(
            painter = painterResource(R.drawable.blood_pressure_device),
            contentDescription = stringResource(R.string.bp_device_image_description),
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
        )
        Text(
            text = stringResource(R.string.bluetooth_instructions),
            fontSize = TextUnit(16f, TextUnitType.Sp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BloodPressurePreview() {
    BloodPressureDeviceInstructions()
}