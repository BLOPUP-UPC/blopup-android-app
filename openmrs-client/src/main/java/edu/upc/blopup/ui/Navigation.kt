package edu.upc.blopup.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import edu.upc.blopup.ui.takingvitals.VitalsViewModel
import edu.upc.blopup.ui.takingvitals.screens.BloodPressureScreen
import edu.upc.blopup.ui.takingvitals.screens.BloodPressureDataScreen
import edu.upc.blopup.ui.takingvitals.screens.HowToActivateBPDeviceScreen
import edu.upc.blopup.vitalsform.Vital

@Composable
fun BloodPressureScreen(navController: NavHostController, viewModel: VitalsViewModel) {
    BloodPressureScreen(navController, viewModel)
}

@Composable
fun HowToActivateBluetoothScreen(navController: NavHostController, viewModel: VitalsViewModel) {
    HowToActivateBPDeviceScreen(navController, viewModel)
}

@Composable
fun BloodPressureDataScreen(navController: NavHostController, vitals: MutableList<Vital>) {
    BloodPressureDataScreen(navController, vitals)
}

@Composable
fun ReceiveWeightDataScreen(navController: NavHostController) {
    ReceiveWeightDataScreen(navController)
}