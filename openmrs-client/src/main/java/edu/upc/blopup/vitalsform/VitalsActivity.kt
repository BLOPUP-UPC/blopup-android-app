package edu.upc.blopup.vitalsform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.upc.R
import edu.upc.blopup.vitalsform.model.BloodPressureDataScreen
import edu.upc.blopup.vitalsform.model.BloodPressureScreen
import edu.upc.blopup.vitalsform.model.HowToActivateBluetoothScreen
import edu.upc.blopup.vitalsform.model.Routes
import edu.upc.blopup.vitalsform.screens.AppToolBarWithMenu
import edu.upc.blopup.vitalsform.screens.ReceiveWeightDataScreen


class VitalsActivity : ComponentActivity() {

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold(
                topBar = { AppToolBarWithMenu(stringResource(R.string.blood_pressure_data)) },
            ) { innerPadding ->
                val navigationController = rememberNavController()
                NavHost(
                    navController = navigationController,
                    startDestination = Routes.BloodPressureScreen.id,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Routes.BloodPressureScreen.id) {
                        BloodPressureScreen(navigationController)
                    }
                    composable(Routes.HowToActivateBluetoothScreen.id) {
                        HowToActivateBluetoothScreen(navigationController)
                    }
                    composable(Routes.BloodPressureDataScreen.id) {
                        BloodPressureDataScreen(navigationController)
                    }
                    composable(Routes.ReceiveWeightDataScreen.id) {
                        ReceiveWeightDataScreen(navigationController)
                    }
                }
            }
        }
    }
}