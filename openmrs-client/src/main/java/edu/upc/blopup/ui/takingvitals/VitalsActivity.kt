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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.ui.BloodPressureDataScreen
import edu.upc.blopup.ui.BloodPressureScreen
import edu.upc.blopup.ui.HowToActivateBluetoothScreen
import edu.upc.blopup.ui.Routes
import edu.upc.blopup.ui.takingvitals.screens.AppToolBarWithMenu
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
                        BloodPressureScreen(navigationController, viewModel)
                    }
                    composable(Routes.HowToActivateBluetoothScreen.id) {
                        HowToActivateBluetoothScreen(navigationController, viewModel)
                    }
                    composable(Routes.BloodPressureDataScreen.id) {
                        BloodPressureDataScreen(navigationController, viewModel)
                    }
                    composable(Routes.ReceiveWeightDataScreen.id) {
                        ReceiveWeightDataScreen(navigationController)
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
