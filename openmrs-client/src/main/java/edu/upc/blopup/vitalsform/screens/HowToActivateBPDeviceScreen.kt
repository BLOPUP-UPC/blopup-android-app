package edu.upc.blopup.vitalsform.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import edu.upc.R


@Composable
fun HowToActivateBPDeviceScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
            ),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        BloodPressureDeviceInstructions()
        ReceiveBloodPressureDataButton(navController)
    }
}

@Composable
fun BloodPressureDeviceInstructions() {
    Column {
        Image(
            painter = painterResource(R.drawable.bp_device_image),
            contentDescription = stringResource(R.string.bp_device_image_description),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 20.dp)
        )
        Text(
            text = instructionsText(),
            fontSize = TextUnit(16f, TextUnitType.Sp),
            lineHeight = 23.sp
        )
    }
}

@Composable
private fun instructionsText() = buildAnnotatedString {
    append(stringResource(R.string.how_to_activate_bluetooth_one))

    withStyle(
        SpanStyle(
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.allergy_orange)
        )
    ) {
        append(stringResource(R.string.how_to_activate_bluetooth_two))
    }

    append(stringResource(R.string.how_to_activate_bluetooth_three))

    withStyle(
        SpanStyle(
            fontWeight = FontWeight.Bold,
            color = Color.Blue
        )
    ) {
        append(stringResource(R.string.how_to_activate_bluetooth_four))
    }

    append(stringResource(R.string.how_to_activate_bluetooth_five))
}

@Preview(showBackground = true)
@Composable
fun BloodPressurePreview() {
    BloodPressureDeviceInstructions()
}