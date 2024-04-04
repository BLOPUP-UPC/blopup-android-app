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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import edu.upc.R
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.databases.entities.LocationEntity

@Composable
fun LocationDialogScreen(
    show: Boolean,
    onDialogClose: () -> Unit,
    viewModel: LocationViewModel = hiltViewModel()
) {

    val currentLocation = viewModel.getLocation()
    val locationsList by viewModel.locationsListResultUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getAllLocations()
    }

    LocationDialog(show, onDialogClose, currentLocation, locationsList, viewModel::setLocation)
}

@Composable
fun LocationDialog(
    show: Boolean,
    onDialogClose: () -> Unit,
    currentLocation: ResultUiState<String>,
    locationsList: ResultUiState<List<LocationEntity>>,
    onSetLocation: (String) -> Unit
) {
    if (show) {
        Dialog(onDismissRequest = { onDialogClose() }) {
            Column(
                Modifier
                    .background(Color.White)
                    .padding(bottom = 8.dp)
            ) {
                LocationDialogTitle()

                CurrentLocation(currentLocation)

                when (currentLocation) {
                    is ResultUiState.Success -> {
                        LocationsMenuAndBottomButtons(
                            onDialogClose,
                            Modifier
                                .padding(top = 10.dp)
                                .align(Alignment.End),
                            currentLocation.data,
                            locationsList,
                            onSetLocation
                        )
                    }

                    ResultUiState.Error -> {}

                    ResultUiState.Loading -> {}
                }
            }
        }
    }
}

@Composable
fun LocationsMenuAndBottomButtons(
    onDialogClose: () -> Unit,
    modifier: Modifier,
    currentLocation: String,
    locationsList: ResultUiState<List<LocationEntity>>,
    onSetLocation: (String) -> Unit
) {

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
    Column(modifier) {
        Row {
            ActionDialogButton(
                { onDialogClose(); onLocationSelected(currentLocation) },
                R.color.dark_grey_for_stroke,
                R.string.dialog_button_cancel
            )
            ActionDialogButton(
                { onDialogClose(); onSetLocation(selectedLocation) },
                R.color.allergy_orange,
                R.string.dialog_button_select_location,
            )
        }
    }
}

@Composable
fun ActionDialogButton(onClick: () -> Unit, buttonColor: Int, buttonText: Int) {
    Button(
        modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp),
        onClick = { onClick() },
        shape = MaterialTheme.shapes.extraSmall,
        contentPadding = PaddingValues(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(buttonColor))
    ) {
        Text(text = stringResource(buttonText).uppercase())
    }

}

@Composable
fun LocationDialogTitle() {
    Column(
        Modifier
            .background(colorResource(id = R.color.dark_teal))
            .fillMaxWidth()
            .padding(15.dp)
    ) {
        Text(
            text = stringResource(R.string.location_dialog_title),
            color = Color.White,
            fontSize = 20.sp
        )
    }
}

@Composable
fun CurrentLocation(location: ResultUiState<String>) {
    when (location) {
        is ResultUiState.Success -> {
            Row(Modifier.padding(top = 25.dp, start = 15.dp, bottom = 10.dp)) {
                Text(
                    text = stringResource(R.string.location_dialog_current_location),
                    color = Color.Gray,
                )
                Text(
                    text = location.data,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        else -> {
            Text(
                text = stringResource(R.string.error_fetching_location),
                color = Color.Red,
                modifier = Modifier.padding(15.dp)
            )
        }
    }

}


@Preview
@Composable
fun LocationDialogPreview() {
    LocationDialog(
        show = true,
        onDialogClose = {},
        currentLocation = ResultUiState.Success("Nursery"),
        locationsList = ResultUiState.Success(
            listOf(
                LocationEntity(display = "Nursery"),
                LocationEntity(display = "Hospital")
            )
        )
    ) {}
}

@Preview
@Composable
fun LocationDialogErrorPreview() {
    LocationDialog(
        show = true,
        onDialogClose = {},
        currentLocation = ResultUiState.Error,
        locationsList = ResultUiState.Error
    ) {}
}
