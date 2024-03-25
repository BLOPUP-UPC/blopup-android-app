package edu.upc.blopup.ui.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import edu.upc.R
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.databases.entities.LocationEntity

@Composable
fun LocationDialogScreen(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    viewModel: LocationViewModel = hiltViewModel()
) {

    val locationsList by viewModel.locationsListResultUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getAllLocations()
    }

    LocationDialog(
        show = show,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        currentLocation = viewModel.getLocation(),
        locationsList =  locationsList
    )
}

@Composable
fun LocationDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    currentLocation: String,
    locationsList: ResultUiState<List<LocationEntity>>
) {
    if (show) {
        Dialog(onDismissRequest = { onDismiss() }) {
            Column(
                Modifier
                    .background(Color.White)
                    .padding(bottom = 8.dp)
            ) {
                Column(
                    Modifier
                        .background(colorResource(id = R.color.dark_teal))
                        .fillMaxWidth()
                        .padding(15.dp)
                ) {
                    Text(
                        text = stringResource(R.string.location_dialog_title),
                        color = Color.White,
                        fontSize = TextUnit(20f, TextUnitType.Sp)
                    )
                }
                Row(Modifier.padding(15.dp)) {
                    Text(
                        text = stringResource(R.string.location_dialog_current_location),
                        color = Color.Gray
                    )
                    Text(
                        text = currentLocation,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                LocationOptions(locationsList, currentLocation)
                Column(
                    Modifier
                        .align(Alignment.End)
                        .padding(top = 10.dp)
                ) {
                    Row {
                        Button(
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp),
                            onClick = { onDismiss() },
                            shape = MaterialTheme.shapes.extraSmall,
                            contentPadding = PaddingValues(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(
                                    R.color.dark_grey_for_stroke
                                )
                            )
                        ) {
                            Text(text = stringResource(R.string.dialog_button_cancel).uppercase())
                        }
                        Button(
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp),
                            onClick = { onConfirm() },
                            shape = MaterialTheme.shapes.extraSmall,
                            contentPadding = PaddingValues(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(
                                    R.color.allergy_orange
                                )
                            )
                        ) {
                            Text(text = stringResource(R.string.dialog_button_select_location).uppercase())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationOptions(locationsList: ResultUiState<List<LocationEntity>>, currentLocation: String) {
    val (selectedLocation, onLocationSelected) = remember { mutableStateOf(currentLocation) }
    Column {
        when (locationsList) {
            is ResultUiState.Success -> {
                locationsList.data.forEach { location ->
                    Row(
                        Modifier
                            .selectable(
                                selected = (location.display == selectedLocation),
                                onClick = {
                                    onLocationSelected(location.display!!)
                                }
                            )
                            .padding(horizontal = 15.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = location.display!!
                        )
                        RadioButton(
                            selected = (location.display == selectedLocation),
                            onClick = { onLocationSelected(location.display!!) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colorResource(id = R.color.allergy_orange)
                            )
                        )
                    }
                }
            }

            else -> {
                Text(
                    text = stringResource(R.string.error_fetching_locations),
                    color = Color.Red
                )
            }

        }
    }
}

@Preview
@Composable
fun LocationDialogPreview() {
    LocationDialog(
        show = true,
        onDismiss = {},
        onConfirm = {},
        currentLocation = "Hospital Clinic",
        ResultUiState.Success(emptyList())
    )
}
