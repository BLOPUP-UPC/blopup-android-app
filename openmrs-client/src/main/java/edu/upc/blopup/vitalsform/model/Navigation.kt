package edu.upc.blopup.vitalsform.model

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import edu.upc.R
import edu.upc.blopup.vitalsform.screens.AppToolBarWithMenu
import edu.upc.blopup.vitalsform.screens.BloodPressureDataScreen
import edu.upc.blopup.vitalsform.screens.BloodPressureScreen
import edu.upc.blopup.vitalsform.screens.HowToActivateBPDeviceScreen

@Composable
fun BloodPressureScreenWithAppBar(navController: NavHostController) {
    Scaffold(
        topBar = { AppToolBarWithMenu(stringResource(R.string.blood_pressure_data)) },
    ) { innerPadding ->
        BloodPressureScreen(innerPadding, navController)
    }
}


@Composable
fun HowToActivateBluetoothScreenWithAppBar(navController: NavHostController) {
    Scaffold(
        topBar = { AppToolBarWithMenu(stringResource(R.string.blood_pressure_data)) },
    ) { innerPadding ->
        HowToActivateBPDeviceScreen(innerPadding, navController)
    }
}

@Composable
fun BloodPressureDataScreenWithAppBar(navController: NavHostController) {
    Scaffold(
        topBar = { AppToolBarWithMenu(stringResource(R.string.blood_pressure_data)) },
    ) { innerPadding ->
        BloodPressureDataScreen(innerPadding, navController)
    }
}