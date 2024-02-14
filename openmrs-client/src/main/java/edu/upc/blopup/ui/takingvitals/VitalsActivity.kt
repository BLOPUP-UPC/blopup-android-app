package edu.upc.blopup.ui.takingvitals

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.ui.Routes
import edu.upc.blopup.ui.takingvitals.screens.AppToolBarWithMenu
import edu.upc.blopup.ui.takingvitals.screens.BloodPressureDataScreen
import edu.upc.blopup.ui.takingvitals.screens.BloodPressureScreen
import edu.upc.blopup.ui.takingvitals.screens.HowToActivateBPDeviceScreen
import edu.upc.blopup.ui.takingvitals.screens.ReceiveWeightDataScreen

@AndroidEntryPoint
class VitalsActivity : ComponentActivity() {

    val viewModel: VitalsViewModel by viewModels()


    private val locationPermission = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @RequiresApi(Build.VERSION_CODES.S)
    private val bluetoothPermission = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askPermissions()

        setContent {
            var topBarTitle by remember { mutableIntStateOf(R.string.blood_pressure_data) }

            Scaffold(
                topBar = { AppToolBarWithMenu(stringResource(topBarTitle)) },
            ) { innerPadding ->

                val navigationController = rememberNavController()
                val uiState by viewModel.uiState.collectAsState()

                NavHost(
                    navController = navigationController,
                    startDestination = Routes.BloodPressureScreen.id,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Routes.BloodPressureScreen.id) {
                        topBarTitle = R.string.blood_pressure_data

                        BloodPressureScreen(
                            { navigationController.navigate(Routes.HowToActivateBluetoothScreen.id) },
                            {
                                viewModel.startListeningBluetoothConnection()
                                navigationController.navigate(Routes.BloodPressureDataScreen.id)
                            }
                        )
                    }
                    composable(Routes.HowToActivateBluetoothScreen.id) {
                        topBarTitle = R.string.blood_pressure_data

                        HowToActivateBPDeviceScreen {
                            viewModel.startListeningBluetoothConnection()
                            navigationController.navigate(Routes.BloodPressureDataScreen.id)
                        }
                    }
                    composable(Routes.BloodPressureDataScreen.id) {
                        topBarTitle = R.string.blood_pressure_data

                        BloodPressureDataScreen(
                            { navigationController.navigate(Routes.ReceiveWeightDataScreen.id) },
                            navigationController::popBackStack,
                            uiState
                        )
                    }
                    composable(Routes.ReceiveWeightDataScreen.id) {
                        topBarTitle = R.string.weight_data

                        ReceiveWeightDataScreen { viewModel.startListeningBluetoothConnection() }
                    }
                }
            }
        }
    }

    private fun askPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, locationPermission, 1)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (
                    ActivityCompat.checkSelfPermission(
                        this,
                        bluetoothPermission[0]
                    ) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(
                                this,
                                bluetoothPermission[1]
                            ) != PackageManager.PERMISSION_GRANTED
                    )
        ) {
            ActivityCompat.requestPermissions(this, bluetoothPermission, 1)
        }
    }
}
