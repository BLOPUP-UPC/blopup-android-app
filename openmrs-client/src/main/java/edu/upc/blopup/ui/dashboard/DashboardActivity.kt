package edu.upc.blopup.ui.dashboard

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.blopup.ui.Routes
import edu.upc.blopup.ui.shared.components.AppBottomNavigationBar
import edu.upc.blopup.ui.shared.components.AppToolBarWithMenu
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.sdk.library.OpenmrsAndroid
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardActivity : ACBaseActivity() {

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            setContent {

                val navigationController = rememberNavController()

                Scaffold(
                    topBar = {
                        AppToolBarWithMenu(
                            "BLOPUP",
                            false,
                            onBackAction = {},
                            this@DashboardActivity::logout,
                            OpenmrsAndroid.getUsername(),
                            false
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
                            DashboardScreen()
                        }
                    }
                }
            }
        }
    }
}
