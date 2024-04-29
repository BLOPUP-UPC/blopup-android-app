package edu.upc.blopup.ui.takingvitals.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.upc.R
import edu.upc.blopup.ui.takingvitals.components.BluetoothButton

@Composable
fun MeasureWeightScreen(onClickReceiveData: () -> Unit, navigateToManualMeasureWeight: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            WeightInstructions()
            InsertWeightManually(navigateToManualMeasureWeight)
        }

        BluetoothButton(onClickReceiveData, R.string.weight_button_label)
    }
}

@Composable
fun InsertWeightManually(navigateToManualMeasureWeight: () -> Unit) {
    Text(
        text = stringResource(R.string.click_here_to_insert_weight_manually), style = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = colorResource(R.color.allergy_orange),
            textDecoration = TextDecoration.Underline
        ),
        modifier = Modifier
            .padding(vertical = 15.dp)
            .clickable { navigateToManualMeasureWeight() }
    )
}

@Composable
fun WeightInstructions() {
    Column {
        Image(
            painter = painterResource(R.drawable.scale_image),
            contentDescription = stringResource(R.string.weight_image_description),
            modifier = Modifier
                .padding(30.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
        Text(
            text = stringResource(R.string.weight_instructions_title),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(R.string.weight_instructions),
            fontSize = 16.sp,
        )
    }
}

@Preview
@Composable
fun WeightDataPreview() {
    MeasureWeightScreen({}) { }
}
