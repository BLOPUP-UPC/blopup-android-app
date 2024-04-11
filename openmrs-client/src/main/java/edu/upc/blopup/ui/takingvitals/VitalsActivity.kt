package edu.upc.blopup.ui.takingvitals

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import edu.upc.blopup.ui.shared.components.AppToolBarWithMenu
import edu.upc.blopup.ui.takingvitals.screens.BloodPressureDataScreen
import edu.upc.blopup.ui.takingvitals.screens.HowToActivateBPDeviceScreen
import edu.upc.blopup.ui.takingvitals.screens.MeasureBloodPressureScreen
import edu.upc.blopup.ui.takingvitals.screens.MeasureHeightScreen
import edu.upc.blopup.ui.takingvitals.screens.MeasureWeightScreen
import edu.upc.blopup.ui.takingvitals.screens.TreatmentAdherenceScreen
import edu.upc.blopup.ui.takingvitals.screens.WeightDataScreen
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.sdk.library.OpenmrsAndroid
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VitalsActivity : ACBaseActivity() {

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
            viewModel.fetchActiveTreatment()
            setContent {

                val navigationController = rememberNavController()

                var topBarTitle by remember { mutableIntStateOf(R.string.blood_pressure_data) }
                var isDataScreen by remember { mutableStateOf(false) }
                val createVisitResultUiState by viewModel.createVisitResultUiState.collectAsState()

                Scaffold(
                    topBar = {
                        AppToolBarWithMenu(
                            stringResource(topBarTitle),
                            onBackAction = {
                                if (!navigationController.popBackStack()) {
                                    finish()
                                }
                            },
                            this@VitalsActivity::logout,
                            OpenmrsAndroid.getUsername(),
                            isDataScreen)
                    },
                ) { innerPadding ->

                    NavHost(
                        navController = navigationController,
                        startDestination = Routes.MeasureBloodPressureScreen.id,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Routes.MeasureBloodPressureScreen.id) {
                            topBarTitle = R.string.blood_pressure_data
                            isDataScreen = false

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
                            isDataScreen = false

                            HowToActivateBPDeviceScreen {
                                viewModel.receiveBloodPressureData()
                                navigationController.navigate(Routes.BloodPressureDataScreen.id)
                            }
                        }
                        composable(Routes.BloodPressureDataScreen.id) {
                            topBarTitle = R.string.blood_pressure_data
                            isDataScreen = true
                            val bloodPressureState by viewModel.bloodPressureUiState.collectAsState()

                            BloodPressureDataScreen(
                                { navigationController.navigate(Routes.MeasureWeightScreen.id) },
                                navigationController::popBackStack,
                                bloodPressureState,
                                viewModel::receiveBloodPressureData
                            )
                        }
                        composable(Routes.MeasureWeightScreen.id) {
                            topBarTitle = R.string.weight_data
                            isDataScreen = false

                            MeasureWeightScreen {
                                viewModel.receiveWeightData()
                                navigationController.navigate(Routes.WeightDataScreen.id)
                            }
                        }
                        composable(Routes.WeightDataScreen.id) {
                            topBarTitle = R.string.weight_data
                            isDataScreen = true
                            val weightState by viewModel.weightUiState.collectAsState()

                            WeightDataScreen(
                                weightState,
                                { navigationController.navigate(Routes.MeasureHeightScreen.id) },
                                navigationController::popBackStack,
                                viewModel::receiveWeightData
                            )
                        }
                        composable(Routes.MeasureHeightScreen.id) {
                            topBarTitle = R.string.height_data
                            isDataScreen = false

                            MeasureHeightScreen({
                                viewModel.saveHeight(it)
                                navigationController.navigate(Routes.TreatmentAdherenceScreen.id)
                            }, viewModel.getLastHeightFromVisits())
                        }
                        composable(Routes.TreatmentAdherenceScreen.id) {
                            topBarTitle = R.string.adherence_data
                            isDataScreen = false
                            val treatmentsResultUiState by viewModel.treatmentsResultUiState.collectAsState()

                            TreatmentAdherenceScreen(
                                { setResultAndFinish(it) },
                                viewModel::createVisit,
                                createVisitResultUiState,
                                treatmentsResultUiState,
                                { lifecycleScope.launch { viewModel.addTreatmentAdherence(it) } },
                                { lifecycleScope.launch { viewModel.fetchActiveTreatment() } }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun askPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.addAll(locationPermission)
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
            permissionsToRequest.addAll(bluetoothPermission)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 1)
        }
    }

    private fun setResultAndFinish(result: Int) {
        setResult(result)
        finish()
    }
}
