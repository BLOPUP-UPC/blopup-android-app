package edu.upc.blopup.vitalsform

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.upc.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppToolBarWithMenu(title: String) {
    val activity = (LocalContext.current as? ComponentActivity)

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(R.color.dark_teal),
            titleContentColor = colorResource(R.color.white),
        ),
        title = {
            Text(title)
        },
        navigationIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back),
                contentDescription = "Back",
                tint = colorResource(R.color.white),
                modifier = Modifier
                    .clickable { activity?.onBackPressedDispatcher?.onBackPressed() }
                    .padding(horizontal = 16.dp)
                    .testTag("back_button")
            )
        }, actions = {

            OptionsMenu()
        }
    )
}

@Composable
fun OptionsMenu() {
    var showMenu by remember { mutableStateOf(false) }
    IconButton(onClick = { showMenu = true }) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = "Options",
            tint = colorResource(R.color.white)
        )
    }
    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        DropdownMenuItem(onClick = {}, text = { Text(text = "Location") })
        DropdownMenuItem(onClick = {}, text = { Text(text = "Settings") })
        DropdownMenuItem(onClick = {}, text = { Text(text = "Contact Us") })
        DropdownMenuItem(onClick = {}, text = { Text(text = "Tutorial") })
        DropdownMenuItem(onClick = {}, text = { Text(text = "About us") })
        DropdownMenuItem(onClick = {}, text = { Text(text = "Log out") })
    }

}

@Preview
@Composable
fun AppToolBarWithMenuPreview() {
    AppToolBarWithMenu(title = "Blood Pressure")
}
