package edu.upc.blopup.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.upc.R
import edu.upc.openmrs.activities.settings.SettingsViewModel
import edu.upc.sdk.utilities.ApplicationConstants.OpenMRSlanguage.LANGUAGE_LIST

@Composable
fun SettingsScreen(
    onOpenPrivacyPolicy: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    SettingsPage(
        viewModel.languageListPosition,
        { viewModel.languageListPosition = it },
        onOpenPrivacyPolicy,
        viewModel.getBuildVersionInfo(context)
    )
}

@Composable
fun SettingsPage(
    languageListPosition: Int,
    setLanguageListPosition: (Int) -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    buildAppString: String,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LanguageSection(languageListPosition, setLanguageListPosition)

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 15.dp),
            thickness = 1.dp,
            color = Color.LightGray
        )

        BlopupAppVersion(buildAppString)

        PrivacyPolicySection(onOpenPrivacyPolicy)
    }
}

@Composable
fun PrivacyPolicySection(onOpenPrivacyPolicy: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 20.dp)) {
        Image(
            painter = painterResource(R.drawable.ic_security_black_24dp),
            contentDescription = "heart icon"
        )
        Text(
            text = stringResource(R.string.settings_privacy_policy),
            fontSize = 16.sp,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable { onOpenPrivacyPolicy() }
        )
    }
}

@Composable
fun BlopupAppVersion(buildAppVersion: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.logo_blopup),
            contentDescription = "BlopUp Logo",
            Modifier.size(60.dp)
        )
        Column(Modifier.padding(horizontal = 10.dp)) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 18.sp,
            )
            Text(
                text = buildAppVersion,
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

    }
}

@Composable
fun LanguageSection(currentLanguagePosition: Int, setLanguagePosition: (Int) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var languageSelected by remember { mutableStateOf(LANGUAGE_LIST[currentLanguagePosition]) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.language), fontSize = 16.sp
        )
        Box(
            modifier = Modifier
                .clickable { showMenu = true }
                .border(
                    width = 1.5.dp,
                    shape = RoundedCornerShape(1.dp),
                    color = Color.Black
                )
                .padding(vertical = 5.dp, horizontal = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = languageSelected,
                    fontSize = 16.sp,
                )
                Icon(
                    Icons.Filled.ArrowDropDown,
                    tint = Color.Black,
                    contentDescription = null,
                )
                ShowLanguagesOptions(showMenu, { showMenu = false }, setLanguagePosition) {
                    languageSelected = it
                }
            }
        }
    }
}

@Composable
fun ShowLanguagesOptions(
    showLanguagesDialog: Boolean,
    closeDropDown: () -> Unit,
    setLanguage: (Int) -> Unit,
    setLanguageSelected: (String) -> Unit,
) {
    DropdownMenu(
        expanded = showLanguagesDialog,
        onDismissRequest = { closeDropDown() },
        modifier = Modifier
            .background(colorResource(R.color.white))
            .padding(5.dp)
    ) {
        LANGUAGE_LIST.forEach { language ->
            DropdownMenuItem(
                onClick = {
                    setLanguageSelected(language)
                    setLanguage(LANGUAGE_LIST.indexOf(language))
                    closeDropDown()
                },
                text = { Text(text = language) })
        }
    }
}

@Preview
@Composable
fun PreviewSettingsScreen() {
    SettingsPage(1, {}, {}, "1.0.0")
}