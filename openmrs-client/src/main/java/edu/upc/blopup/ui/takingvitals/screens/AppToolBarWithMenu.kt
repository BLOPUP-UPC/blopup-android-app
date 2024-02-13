package edu.upc.blopup.ui.takingvitals.screens

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.upc.R
import edu.upc.openmrs.activities.community.contact.AboutActivity
import edu.upc.openmrs.activities.community.contact.ContactUsActivity
import edu.upc.openmrs.activities.introduction.IntroActivity
import edu.upc.openmrs.activities.settings.SettingsActivity
import edu.upc.sdk.library.OpenmrsAndroid

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
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    IconButton(onClick = { showMenu = true }) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = "Options",
            tint = colorResource(R.color.white)
        )
    }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
        modifier = Modifier
            .background(colorResource(R.color.white))
            .padding(end = 35.dp)
    ) {
        DropdownMenuItem(
            onClick = {
                context.startActivity(
                    Intent(
                        context,
                        SettingsActivity::class.java
                    )
                ); showMenu = false
            },
            text = {
                Text(
                    text = stringResource(R.string.action_settings),
                    style = TextStyle(fontWeight = FontWeight.Normal)
                )
            })
        DropdownMenuItem(
            onClick = {
                context.startActivity(
                    Intent(
                        context,
                        ContactUsActivity::class.java
                    )
                ); showMenu = false
            },
            text = {
                Text(
                    text = stringResource(R.string.contact_us_text),
                    style = TextStyle(fontWeight = FontWeight.Normal)
                )
            })
        DropdownMenuItem(
            onClick = {
                context.startActivity(Intent(context, IntroActivity::class.java))
                OpenmrsAndroid.setUserFirstTime(true)
                showMenu = false
            },
            text = {
                Text(
                    text = stringResource(R.string.app_tutorial),
                    style = TextStyle(fontWeight = FontWeight.Normal)
                )
            })
        DropdownMenuItem(
            onClick = {
                context.startActivity(
                    Intent(
                        context,
                        AboutActivity::class.java
                    )
                )
                showMenu = false
            },
            text = {
                Text(
                    text = stringResource(R.string.action_about),
                    style = TextStyle(fontWeight = FontWeight.Normal)
                )
            })
    }
}

@Preview
@Composable
fun AppToolBarWithMenuPreview() {
    AppToolBarWithMenu(title = "Blood Pressure")
}