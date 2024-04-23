package edu.upc.blopup.ui.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.ui.Routes
import edu.upc.blopup.ui.createpatient.CreatePatientScreen
import edu.upc.blopup.ui.searchpatient.SearchPatientScreen
import edu.upc.blopup.ui.shared.components.AppBottomNavigationBar
import edu.upc.blopup.ui.shared.components.AppToolBarWithMenu
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.openmrs.activities.addeditpatient.countryofbirth.Country
import edu.upc.openmrs.activities.patientdashboard.PatientDashboardActivity
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ToastUtil
import kotlinx.coroutines.launch
import java.util.Locale

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
                var isSearchPatientScreen by remember { mutableStateOf(false) }
                var topBarTitle by remember { mutableIntStateOf(R.string.organization_name) }
                var searchQuery by remember { mutableStateOf("") }

                Scaffold(
                    topBar = {
                        AppToolBarWithMenu(
                            stringResource(topBarTitle),
                            onBackAction = {
                                if (!navigationController.popBackStack()) {
                                    finish()
                                }
                            },
                            this@DashboardActivity::logout,
                            OpenmrsAndroid.getUsername(),
                            showBackButtonInMenu,
                            isCreatePatientWithSomeInput = isCreatePatientWithSomeInput,
                            isSearchPatientScreen = isSearchPatientScreen,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it }
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
                            topBarTitle = R.string.organization_name
                            isSearchPatientScreen = false
                            showBackButtonInMenu = false
                            DashboardScreen { navigationController.navigate(Routes.CreatePatientScreen.id) }
                        }
                        composable(Routes.SearchPatientScreen.id) {
                            topBarTitle = R.string.action_synced_patients
                            isSearchPatientScreen = true
                            showBackButtonInMenu = true
                            SearchPatientScreen(
                                { patientId, patientUuid ->
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
                                },
                                searchQuery,
                                { ToastUtil.error(getString(R.string.patient_has_been_removed)) }
                            )
                        }
                        composable(Routes.CreatePatientScreen.id) {
                            topBarTitle = R.string.action_register_patient
                            isSearchPatientScreen = false
                            showBackButtonInMenu = true
                            CreatePatientScreen(
                                { patientId, patientUuid ->
                                    navigationController.navigate(
                                        Routes.PatientDashboardScreen.id.replace(
                                            "{${ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE}}",
                                            patientId.toString()
                                        ).replace(
                                            "{${ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE}}",
                                            patientUuid
                                        )
                                    ) {
                                        popUpTo(Routes.DashboardScreen.id)
                                    }
                                },
                                { isCreatePatientWithSomeInput = it },
                                { askPermissionsForLegalConsent() },
                                { getString(it) },
                                { country: Country -> country.getLabel(this@DashboardActivity) },
                                { selectedLanguage: String, resourceId: Int ->
                                    getTextInLanguageSelected(
                                        selectedLanguage,
                                        resourceId,
                                        this@DashboardActivity
                                    )
                                },
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
                            isCreatePatientWithSomeInput = false
                            isSearchPatientScreen = false
                            showBackButtonInMenu = true
                            PatientDashboardScreen(
                                { patientId, patientUuid ->
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
                                },
                                it.arguments?.getLong(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE)
                                    ?: 0,
                                it.arguments?.getString(ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE)
                                    ?: ""
                            )
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
fun PatientDashboardScreen(
    startPatientDashboardActivity: (patientId: Long, patientUuid: String) -> Unit,
    patientId: Long,
    patientUuid: String
) {
    Text("Patient $patientId $patientUuid")
    LaunchedEffect(true) {
        startPatientDashboardActivity(patientId, patientUuid)
    }
}

private fun getTextInLanguageSelected(
    selectedLanguage: String,
    resourceId: Int,
    context: Context
): String {
    val requestedLocale = Locale(selectedLanguage)
    val config =
        Configuration(context.resources.configuration).apply { setLocale(requestedLocale) }

    return context.createConfigurationContext(config).getText(resourceId).toString()
}

