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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import edu.upc.R

@Composable
fun SettingsScreen(onOpenPrivacyPolicy: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)) {

        LanguageSection()

        HorizontalDivider(modifier = Modifier.padding(vertical = 15.dp), thickness = 1.dp, color = Color.LightGray)

        BlopupAppVersion()

        RateUsAndPrivacyPolicySection(onOpenPrivacyPolicy)

    }

}

@Composable
fun RateUsAndPrivacyPolicySection(onOpenPrivacyPolicy: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 20.dp)) {
        Icon(imageVector = Icons.Filled.Favorite, contentDescription = "heart icon", tint = Color.DarkGray )
        Text(
            text = stringResource(R.string.rate_us_string),
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 15.dp).clickable {  }
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 20.dp)) {
        Image(painter = painterResource(R.drawable.ic_security_black_24dp), contentDescription = "heart icon" )
        Text(
            text = stringResource(R.string.settings_privacy_policy),
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp).clickable {onOpenPrivacyPolicy()}
        )
    }
}

@Composable
fun BlopupAppVersion() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(R.drawable.logo_blopup), contentDescription = "BlopUp Logo")
        Column(Modifier.padding(horizontal = 10.dp)) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 20.sp,
            )
            Text(
                text = "1.9.12Build0",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

    }}

@Composable
fun LanguageSection() {
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
                .clickable { }
                .border(
                    width = 1.5.dp,
                    shape = RoundedCornerShape(1.dp),
                    color = Color.Black
                )
                .padding(vertical = 5.dp, horizontal = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "English",
                    fontSize = 16.sp
                )
                Icon(
                    Icons.Filled.ArrowDropDown,
                    tint = Color.Black,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
fun ShowLanguagesOptions(
    showLanguagesDialog: Boolean,
    closeDropDown: () -> Unit,
    onLanguageSelected: (String) -> Unit,
) {
    val context = LocalContext.current
    val languagesList = context.resources.getStringArray(R.array.languages)
    DropdownMenu(
        expanded = showLanguagesDialog,
        onDismissRequest = { closeDropDown() },
        modifier = Modifier
            .background(colorResource(R.color.white))
            .padding(5.dp)
    ) {
        languagesList.forEach { language ->
            DropdownMenuItem(
                onClick = {
                    onLanguageSelected(language)
                    closeDropDown()
                },
                text = { Text(text = language, color = Color.Gray) })
        }
    }
}

@Preview
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen {}
}