package edu.upc.blopup.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import edu.upc.blopup.ui.takingvitals.screens.BloodPressureDataScreen
import edu.upc.blopup.ui.takingvitals.screens.BloodPressureScreen
import edu.upc.blopup.ui.takingvitals.screens.HowToActivateBPDeviceScreen

@Composable
fun BloodPressureScreen(navController: NavHostController) {
    BloodPressureScreen(navController)
}

@Composable
fun HowToActivateBluetoothScreen(navController: NavHostController) {
    HowToActivateBPDeviceScreen(navController)
}

@Composable
fun BloodPressureDataScreen(navController: NavHostController) {
    BloodPressureDataScreen(navController)
}

@Composable
fun Routes.ReceiveWeightDataScreen(navController: NavHostController) {
    ReceiveWeightDataScreen(navController)
}