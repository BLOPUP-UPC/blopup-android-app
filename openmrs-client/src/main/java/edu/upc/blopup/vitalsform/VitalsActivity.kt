package edu.upc.blopup.vitalsform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.upc.blopup.vitalsform.model.BloodPressureDataScreenWithAppBar
import edu.upc.blopup.vitalsform.model.BloodPressureScreenWithAppBar
import edu.upc.blopup.vitalsform.model.HowToActivateBluetoothScreenWithAppBar
import edu.upc.blopup.vitalsform.model.Routes


class VitalsActivity : ComponentActivity() {

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navigationController = rememberNavController()
            NavHost(
                navController = navigationController,
                startDestination = Routes.BloodPressureScreen.id
            ) {
                composable(Routes.BloodPressureScreen.id) {
                    BloodPressureScreenWithAppBar(navigationController)
                }
                composable(Routes.HowToActivateBluetoothScreen.id) {
                    HowToActivateBluetoothScreenWithAppBar(navigationController)
                }
                composable(Routes.BloodPressureDataScreen.id) {
                    BloodPressureDataScreenWithAppBar(navigationController)
                }
            }
        }
    }
}