package edu.upc.blopup.vitalsform.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import edu.upc.R
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.BloodPressureViewState
import edu.upc.blopup.toggles.check
import edu.upc.blopup.toggles.hardcodeBluetoothDataToggle
import edu.upc.blopup.vitalsform.Vital
import edu.upc.blopup.vitalsform.VitalsActivity
import edu.upc.blopup.vitalsform.model.Routes
import edu.upc.openmrs.utilities.observeOnce
import edu.upc.sdk.utilities.ApplicationConstants


@Composable
fun BloodPressureScreen(navController: NavHostController) {
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
        BloodPressureInstructions()
        HowToActivateTheDeviceButton(navController)
        ReceiveBloodPressureDataButton(navController)
    }
}

@Composable
fun BloodPressureInstructions() {
    Column {
        Image(
            painter = painterResource(R.drawable.how_to_measure),
            contentDescription = stringResource(R.string.bp_image_description),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 50.dp)
        )
        Column {
            Text(
                text = stringResource(R.string.blood_pressure_instructions_title),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(20f, TextUnitType.Sp)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.blood_pressure_instructions_two),
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )

        }
    }

}


@Composable
fun HowToActivateTheDeviceButton(navController: NavHostController) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
        .fillMaxWidth()
        .clickable { navController.navigate(Routes.HowToActivateBluetoothScreen.id) }) {
        Row {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.icon_bp_info),
                contentDescription = "Info",
                modifier = Modifier.padding(end = 8.dp),
                tint = colorResource(R.color.allergy_orange)
            )
            Text(
                text = stringResource(R.string.how_to_activate_the_device_label),
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Info",
            tint = colorResource(R.color.dark_grey_for_stroke)
        )
    }
}

@Composable
fun ReceiveBloodPressureDataButton(navController: NavHostController) {
    val vitalsActivity = LocalContext.current as VitalsActivity
    Button(
        shape = MaterialTheme.shapes.extraSmall,
        onClick = { startReading(vitalsActivity, navController) },
        contentPadding = PaddingValues(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(
                R.color.bluetooth_blue
            )
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.blood_pressure_button_label),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(16f, TextUnitType.Sp)
                )
            )
            Icon(
                painter = painterResource(id = android.R.drawable.stat_sys_data_bluetooth),
                contentDescription = "Bluetooth icon"
            )
        }

    }
}

private fun startReading(activity: VitalsActivity, navController: NavHostController) {
    hardcodeBluetoothDataToggle.check(onToggleEnabled = {
        hardcodeBluetoothData(activity)
    })

    activity.viewModel.startListeningBluetoothConnection()
    observeBloodPressureData(activity, navController)
}

private fun hardcodeBluetoothData(activity: VitalsActivity) {
    activity.updateVitals(
        mutableListOf(
            Vital(
                ApplicationConstants.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT,
                (80..250).random().toString()
            ),
            Vital(
                ApplicationConstants.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT,
                (50..99).random().toString()
            ),
            Vital(
                ApplicationConstants.VitalsConceptType.HEART_RATE_FIELD_CONCEPT,
                (55..120).random().toString()
            )
        )
    )
}


private fun observeBloodPressureData(activity: VitalsActivity, navController: NavHostController) {
    activity.viewModel.viewState.observeOnce(activity) { state ->
        when (state) {
            is BloodPressureViewState.Content -> {
                activity.updateVitals(
                    mutableListOf(
                        Vital(
                            ApplicationConstants.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT,
                            state.measurement.systolic.toString()
                        ),
                        Vital(
                            ApplicationConstants.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT,
                            state.measurement.diastolic.toString()
                        ),
                        Vital(
                            ApplicationConstants.VitalsConceptType.HEART_RATE_FIELD_CONCEPT,
                            state.measurement.heartRate.toString()
                        )
                    )
                )
            }

            else -> {}
        }
    }
    navController.navigate(Routes.BloodPressureDataScreen.id)
}


@Preview
@Composable
fun Preview() {
    BloodPressureScreen(navController = NavHostController(LocalContext.current))
}