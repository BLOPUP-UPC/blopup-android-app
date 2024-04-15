package edu.upc.blopup.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.blopup.ui.Routes
import edu.upc.blopup.ui.addeditpatient.CreatePatientScreen
import edu.upc.blopup.ui.shared.components.AppBottomNavigationBar
import edu.upc.blopup.ui.shared.components.AppToolBarWithMenu
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardActivity
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.utilities.ApplicationConstants
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardActivity : ACBaseActivity() {

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            setContent {
                val navigationController = rememberNavController()
                var showBackButtonInMenu by remember { mutableStateOf(false) }
                var isCreatePatientWithSomeInput by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        AppToolBarWithMenu(
                            "BLOPUP",
                            onBackAction = {
                                if (!navigationController.popBackStack()) {
                                    finish()
                                }
                            },
                            this@DashboardActivity::logout,
                            OpenmrsAndroid.getUsername(),
                            showBackButtonInMenu,
                            isCreatePatientWithSomeInput = isCreatePatientWithSomeInput
                        )
                    },
                    bottomBar = {
                        AppBottomNavigationBar(navigationController)
                    }
                ) { innerPadding ->

                    NavHost(
                        navController = navigationController,
                        startDestination = Routes.DashboardScreen.id,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Routes.DashboardScreen.id) {
                            showBackButtonInMenu = false
                            DashboardScreen ()
                        }
                        composable(Routes.CreatePatientScreen.id) {
                            showBackButtonInMenu = true
                            CreatePatientScreen(
                                { patientId, patientUuid ->
                                    navigationController.navigate(
                                        Routes.PatientDashboardScreen.id.replace(
                                            "{${ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE}}",
                                            patientId.toString()
                                        ).replace("{${ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE}}", patientUuid)
                                    ) {
                                        popUpTo(Routes.DashboardScreen.id)
                                    }
                                },
                                { isCreatePatientWithSomeInput = true },
                                { askPermissionsForLegalConsent() }
                            )
                        }
                        composable(
                            Routes.PatientDashboardScreen.id, arguments = listOf(
                                navArgument(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE) {
                                    type = NavType.LongType
                                },
                                navArgument(ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE) {
                                    type = NavType.StringType
                                },
                            )
                        ) {
                            showBackButtonInMenu = true
                            PatientDashboardScreen ({ patientId, patientUuid ->
                                startActivity(
                                    Intent(
                                        this@DashboardActivity,
                                        PatientDashboardActivity::class.java
                                    ).apply {
                                        putExtra(
                                            ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE,
                                            patientId
                                        )
                                        putExtra(
                                            ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE,
                                            patientUuid
                                        )
                                    })
                            }, it.arguments?.getLong(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE) ?: 0,
                                it.arguments?.getString(ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE) ?: "")
                        }
                    }
                }
            }
        }
    }

    private fun askPermissionsForLegalConsent() {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
            ||
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                ), 200
            )
        }
    }
}

@Composable
fun PatientDashboardScreen(startPatientDashboardActivity: (patientId: Long, patientUuid: String) -> Unit, patientId: Long, patientUuid: String) {
    Text("Patient $patientId $patientUuid")
    LaunchedEffect(true) {
        startPatientDashboardActivity(patientId, patientUuid)
    }
}
