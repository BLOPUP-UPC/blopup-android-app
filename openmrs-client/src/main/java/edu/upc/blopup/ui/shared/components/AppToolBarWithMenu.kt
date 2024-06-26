package edu.upc.blopup.ui.shared.components

import SearchInput
import SearchOptionIcon
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.upc.R
import edu.upc.blopup.ui.location.LocationDialogScreen

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppToolBarWithMenu(
    title: String,
    onBackAction: () -> Unit,
    onLogout: () -> Unit,
    username: String,
    showGoBackButton: Boolean = true,
    isDataScreen: Boolean = false,
    isCreatePatientWithSomeInput: Boolean = false,
    isSearchPatientScreen: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    navigateToSettings: () -> Unit = {}
) {
    var showLoseDataDialog by remember { mutableStateOf(false) }
    var isSearchInput by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(R.color.dark_teal),
            titleContentColor = colorResource(R.color.white),
        ),
        title = {
            if(isSearchInput && isSearchPatientScreen) {
                SearchInput(searchQuery) { onSearchQueryChange(it) }
            } else {
                Text(title)
            }
        },
        navigationIcon = {
            if (showGoBackButton) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = colorResource(R.color.white),
                    modifier = Modifier
                        .clickable {
                            if (isDataScreen || isCreatePatientWithSomeInput) {
                                showLoseDataDialog = true
                            } else {
                                onBackAction()
                            }
                        }
                        .padding(horizontal = 16.dp)
                        .testTag("back_button")
                )
            }
        }, actions = {
            if (isSearchPatientScreen) {
                SearchOptionIcon ({ isSearchInput = true }, isSearchInput) { isSearchInput = false; onSearchQueryChange("") }
            }
            OptionsMenu(onLogout, username, navigateToSettings)
        }
    )

    if (isCreatePatientWithSomeInput || isDataScreen) {
        BackHandler {
            showLoseDataDialog = true
        }
    }

    AppDialog(
        show = showLoseDataDialog,
        onDismiss = { showLoseDataDialog = false },
        onConfirm = { showLoseDataDialog = false; onBackAction() },
        title = if (isDataScreen) R.string.remove_vitals else R.string.dialog_title_reset_patient,
        messageDialog = if (isDataScreen) R.string.cancel_vitals_dialog_message else R.string.dialog_message_data_lost,
        onDismissText = if (isDataScreen) R.string.keep_vitals_dialog_message else R.string.dialog_button_stay,
        onConfirmText = if (isDataScreen) R.string.end_vitals_dialog_message else R.string.dialog_button_leave
    )
}

@Composable
fun OptionsMenu(onLogout: () -> Unit, username: String, navigateToSettings: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val showLocationDialog = remember { mutableStateOf(false) }
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
            onClick = { navigateToSettings(); showMenu = false },
            text = {
                Text(
                    text = stringResource(R.string.action_settings),
                    style = TextStyle(fontWeight = FontWeight.Normal)
                )
            })
        DropdownMenuItem(
            onClick = {
                showLocationDialog.value = true
                showMenu = false
            },
            text = {
                Text(
                    text = stringResource(R.string.action_location),
                    style = TextStyle(fontWeight = FontWeight.Normal)
                )
            })
        DropdownMenuItem(
            onClick = {
                showDialog.value = true
                showMenu = false
            },
            text = {
                Text(
                    text = stringResource(R.string.action_logout) + " " + username,
                    style = TextStyle(fontWeight = FontWeight.Normal)
                )
            })
    }
    AppDialog(
        show = showDialog.value,
        title = R.string.logout_dialog_title,
        messageDialog = R.string.logout_dialog_message,
        onDismissText = R.string.dialog_button_cancel,
        onConfirmText = R.string.logout_dialog_button,
        onDismiss = { showDialog.value = false },
        onConfirm = {
            onLogout()
            showDialog.value = false
        },
    )
    LocationDialogScreen(
        show = showLocationDialog.value,
        onDialogClose = { showLocationDialog.value = false }
    )
}

@Preview
@Composable
fun AppToolBarWithMenuPreview() {
    AppToolBarWithMenu(
        title = "Blood Pressure",
        isDataScreen = false,
        onBackAction = {},
        onLogout = {},
        username = "Jane Doe"
    )
}