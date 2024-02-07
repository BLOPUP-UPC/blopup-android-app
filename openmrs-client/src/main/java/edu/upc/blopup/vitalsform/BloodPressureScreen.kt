package edu.upc.blopup.vitalsform

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import edu.upc.R

@Composable
fun BloodPressureScreenWithAppBar() {
    Scaffold(
        topBar = { TopBar() },
    ) { innerPadding ->
        BloodPressureScreen(innerPadding)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopBar() {

    val activity = (LocalContext.current as? VitalsActivity)

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(R.color.dark_teal),
            titleContentColor = colorResource(R.color.white),
        ),
        title = {
            Text("Blood Pressure Data")
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
        }
    )
}

@Composable
fun BloodPressureScreen(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        BloodPressureInstructions()
        HowToActivateTheDeviceButton()
        ReceiveBloodPressureDataButton()
    }
}

@Composable
fun BloodPressureInstructions() {
    Column {
        Image(
            painter = painterResource(R.drawable.how_to_measure),
            contentDescription = stringResource(R.string.bp_image_description),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 50.dp)
        )
        Column {
            Text(
                text = stringResource(R.string.blood_pressure_instructions_title),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(16f, TextUnitType.Sp)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.blood_pressure_instructions_two),
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )

        }
    }

}


@Composable
fun HowToActivateTheDeviceButton() {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
        .fillMaxWidth()
        .clickable { /*TODO*/ }) {
        Row {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.icon_bp_info),
                contentDescription = "Info",
                modifier = Modifier.padding(end = 8.dp),
                tint = colorResource(R.color.allergy_orange)
            )
            Text(
                text = stringResource(R.string.how_to_activate_the_device_label),
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Info",
            tint = colorResource(R.color.dark_grey_for_stroke)
        )
    }
}

@Composable
fun ReceiveBloodPressureDataButton() {
    Button(
        shape = MaterialTheme.shapes.extraSmall,
        onClick = { /*TODO*/ },
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(
                R.color.bluetooth_blue
            )
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.blood_pressure_button_label),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(16f, TextUnitType.Sp)
                )
            )
            Icon(
                painter = painterResource(id = android.R.drawable.stat_sys_data_bluetooth),
                contentDescription = "Bluetooth icon"
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun BloodPressurePreview() {
    BloodPressureScreenWithAppBar()
}