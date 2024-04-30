package edu.upc.blopup.ui.createpatient.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.upc.R
import edu.upc.blopup.ui.createpatient.StructureLabelText

@Composable
fun LegalConsentSection(
    setLegalConsentFile: (String) -> Unit,
    legalConsentFile: String,
    getStringByResourceId: (Int) -> String,
    getTextInLanguageSelected: (String, Int) -> String,
) {
    var showLanguagesDropDownList by remember { mutableStateOf(false) }
    var showLegalConsentDialog by remember { mutableStateOf(false) }
    var languageSelected by remember { mutableStateOf(getStringByResourceId(R.string.select_language)) }


    Column(Modifier.padding(vertical = 15.dp)) {
        StructureLabelText(R.string.record_patient_consent)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clickable { showLanguagesDropDownList = true }
                    .border(
                        width = 1.dp,
                        color = if (languageSelected == getStringByResourceId(R.string.select_language)) MaterialTheme.colorScheme.error else Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(15.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 15.dp)
                    ) {
                        Text(
                            text = languageSelected,
                            fontSize = 16.sp,
                            color = if (languageSelected == getStringByResourceId(R.string.select_language)) MaterialTheme.colorScheme.error else Color.Black,
                        )
                    }
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        tint = Color.Gray,
                        contentDescription = null,
                    )
                }
                ShowLanguagesDropDownList(
                    showLanguagesDropDownList,
                    { showLanguagesDropDownList = false },
                    { language -> languageSelected = language })
            }
            if (legalConsentFile.isNotEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.saved_recording_icon),
                    contentDescription = "Saved recording icon",
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                        .size(35.dp),
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.allergy_orange)),
                )
            }

        }

        Text(
            text = if (legalConsentFile.isEmpty()) stringResource(id = R.string.record_legal_consent_u) else stringResource(
                id = R.string.record_again_legal_consent
            ),
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .clickable {
                    if (languageSelected != getStringByResourceId(R.string.select_language)) {
                        showLegalConsentDialog = true
                    }
                },
            color = if (languageSelected == getStringByResourceId(R.string.select_language)) Color.Gray else colorResource(
                R.color.allergy_orange
            )
        )
        if (showLegalConsentDialog) {
            LegalConsentDialog(
                languageSelected,
                { showLegalConsentDialog = false },
                { setLegalConsentFile(it) },
                legalConsentFile,
                getStringByResourceId,
                getTextInLanguageSelected
            )
        }
    }
}

@Composable
fun ShowLanguagesDropDownList(
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