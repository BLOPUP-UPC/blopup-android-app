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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.ui.Routes
import edu.upc.blopup.ui.takingvitals.screens.AppToolBarWithMenu
import edu.upc.blopup.ui.takingvitals.screens.BloodPressureDataScreen
import edu.upc.blopup.ui.takingvitals.screens.HowToActivateBPDeviceScreen
import edu.upc.blopup.ui.takingvitals.screens.MeasureBloodPressureScreen
import edu.upc.blopup.ui.takingvitals.screens.MeasureHeightScreen
import edu.upc.blopup.ui.takingvitals.screens.MeasureWeightScreen
import edu.upc.blopup.ui.takingvitals.screens.TreatmentAdherenceScreen
import edu.upc.blopup.ui.takingvitals.screens.WeightDataScreen
import kotlinx.coroutines.launch
import rx.android.schedulers.AndroidSchedulers

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

        lifecycleScope.launch {
            val treatments = viewModel.fetchActiveTreatment()
            setContent {
                var topBarTitle by remember { mutableIntStateOf(R.string.blood_pressure_data) }

                Scaffold(
                    topBar = { AppToolBarWithMenu(stringResource(topBarTitle)) },
                ) { innerPadding ->

                    val navigationController = rememberNavController()
                    val uiState by viewModel.vitalsUiState.collectAsState()

                    NavHost(
                        navController = navigationController,
                        startDestination = Routes.MeasureBloodPressureScreen.id,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Routes.MeasureBloodPressureScreen.id) {
                            topBarTitle = R.string.blood_pressure_data

                            MeasureBloodPressureScreen(
                                { navigationController.navigate(Routes.HowToActivateBluetoothScreen.id) },
                                {
                                    viewModel.receiveBloodPressureData()
                                    navigationController.navigate(Routes.BloodPressureDataScreen.id)
                                }
                            )
                        }
                        composable(Routes.HowToActivateBluetoothScreen.id) {
                            topBarTitle = R.string.blood_pressure_data

                            HowToActivateBPDeviceScreen {
                                viewModel.receiveBloodPressureData()
                                navigationController.navigate(Routes.BloodPressureDataScreen.id)
                            }
                        }
                        composable(Routes.BloodPressureDataScreen.id) {
                            topBarTitle = R.string.blood_pressure_data

                            BloodPressureDataScreen(
                                { navigationController.navigate(Routes.MeasureWeightScreen.id) },
                                navigationController::popBackStack,
                                uiState
                            )
                        }
                        composable(Routes.MeasureWeightScreen.id) {
                            topBarTitle = R.string.weight_data

                            MeasureWeightScreen {
                                viewModel.receiveWeightData()
                                navigationController.navigate(Routes.WeightDataScreen.id)
                            }
                        }
                        composable(Routes.WeightDataScreen.id) {
                            topBarTitle = R.string.weight_data

                            WeightDataScreen(
                                { navigationController.navigate(Routes.MeasureHeightScreen.id) },
                                navigationController::popBackStack,
                                uiState
                            )
                        }
                        composable(Routes.MeasureHeightScreen.id) {
                            topBarTitle = R.string.height_data

                            MeasureHeightScreen({
                                viewModel.saveHeight(it)
                                navigationController.navigate(Routes.TreatmentAdherenceScreen.id)
                            }, viewModel.getLastHeightFromVisits())
                        }
                        composable(Routes.TreatmentAdherenceScreen.id) {
                            topBarTitle = R.string.adherence_data

                            TreatmentAdherenceScreen(
                                { createVisitAndFinishActivity() },
                                treatments
                            )
                        }
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

    private fun createVisitAndFinishActivity() {
        viewModel.createVisit()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                setResult(RESULT_OK)
                finish()
            }
    }
}
