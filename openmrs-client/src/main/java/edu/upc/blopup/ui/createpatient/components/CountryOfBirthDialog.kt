package edu.upc.blopup.ui.createpatient.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import edu.upc.R
import edu.upc.openmrs.activities.editpatient.countryofbirth.Country

@Composable
fun CountryOfBirthDialog(
    onCloseDialog: () -> Unit,
    onCountrySelected: (Country) -> Unit,
    getCountryLabel: (Country) -> String
) {

    var searchInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onCloseDialog() }) {
        Column(
            Modifier
                .background(Color.White)
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.country_of_birth_default))
                Icon(Icons.Filled.Close, contentDescription = "close dialog icon")
            }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { onCloseDialog() },
                value = searchInput,
                onValueChange = { searchInput = it },
                label = { Text(text = stringResource(R.string.search_hint_text)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.primary),
                    focusedLabelColor = colorResource(R.color.primary),
                    cursorColor = Color.Black
                )
            )
            LazyColumn {
                val countryList = Country.entries.sortedBy { getCountryLabel(it) }
                items(countryList.filter { getCountryLabel(it).contains(searchInput, true)}) {
                    ListItem(it, getCountryLabel(it), onItemSelected = { onCountrySelected(it); onCloseDialog() })
                }
            }
        }

    }

}

@Composable
fun ListItem(country: Country, countryLabel: String, onItemSelected: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp).clickable { onItemSelected() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(country.flag),
            contentDescription = "country flag",
            modifier = Modifier.padding(end = 10.dp)
        )
        Text(text = countryLabel, color = Color.Gray, fontSize = 16.sp)
    }
}

@Composable
fun CountryOfBirthField(countryOfBirth: String, onShowCountryOfBirthDialog: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onShowCountryOfBirthDialog() }
            .border(
                width = 1.dp,
                color = if (countryOfBirth.isEmpty()) MaterialTheme.colorScheme.error else Color.Gray,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 16.dp, vertical = 15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = countryOfBirth.ifEmpty { stringResource(R.string.country_of_birth_default) },
                fontSize = 16.sp,
                color = if (countryOfBirth.isEmpty()) MaterialTheme.colorScheme.error else Color.Black,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Filled.ArrowDropDown,
                tint = Color.Gray,
                contentDescription = null
            )
        }
    }
}

@Preview
@Composable
fun CountryOfBirthDialogPreview() {
    CountryOfBirthDialog({}, {}, { it.name })
}

@Preview
@Composable
fun ListItemPreview() {
    ListItem(Country.ARGENTINA, "") {}
}