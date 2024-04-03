package edu.upc.blopup.ui.shared.components

import android.content.Intent
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import edu.upc.R

@Composable
fun AppBottomNavigationBar(navController: NavController) {
    val context = LocalContext.current

    val menuItems = listOf(
        ItemsBottomNav.Home,
        ItemsBottomNav.Search,
        ItemsBottomNav.Register
    )

    BottomAppBar(Modifier.height(60.dp), containerColor = colorResource(R.color.primary)) {
        val backStackEntry = navController.currentBackStackEntryAsState()

        NavigationBar(containerColor = colorResource(R.color.primary)) {
            menuItems.forEach {
                val currentRoute = backStackEntry.value?.destination?.route
                val selected = currentRoute == it.route
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        context.startActivity(Intent(context, Class.forName(it.route)))
                    },
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.description,
                            tint = if(selected) Color.White else Color.LightGray
                        )
                    },
                    label = {
                        Text(
                            stringResource(it.label),
                            color = if(selected) Color.White else Color.LightGray
                        )
                    },
                )
            }
        }
    }
}

sealed class ItemsBottomNav(val icon: ImageVector, val label: Int, val description:String,  val route: String) {
    data object Home : ItemsBottomNav(Icons.Outlined.Home, R.string.bottom_navigation_option_home, "Menu icon","edu.upc.blopup.ui.dashboard.DashboardActivity")
    data object Search : ItemsBottomNav(Icons.Default.Search, R.string.bottom_navigation_option_search, "Search icon", "edu.upc.openmrs.activities.syncedpatients.SyncedPatientsActivity")
    data object Register : ItemsBottomNav(Icons.Outlined.AccountCircle, R.string.bottom_navigation_option_register, "Person icon", "edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity")
}

@Preview
@Composable
fun AppBottomNavigationBarPreview() {
    AppBottomNavigationBar(navController = NavController(LocalContext.current))
}