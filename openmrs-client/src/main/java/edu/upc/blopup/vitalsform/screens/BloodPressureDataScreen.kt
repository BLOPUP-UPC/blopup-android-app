package edu.upc.blopup.vitalsform.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import edu.upc.R
import edu.upc.blopup.vitalsform.VitalsActivity
import edu.upc.blopup.vitalsform.model.Routes
import edu.upc.sdk.utilities.ApplicationConstants

@Composable
fun BloodPressureDataScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DataReceivedSuccessfully()
        BloodPressureValues()
        NavigationButtons(navController)
    }

}

@Composable
private fun DataReceivedSuccessfully() {
    Column(
        modifier = Modifier.padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.circle_green_checkmark),
            contentDescription = "Large white tick in a green circle."
        )
        Text(
            "Data Received Successfully.",
            fontSize = TextUnit(20F, TextUnitType.Sp),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
fun BloodPressureValues() {
    val activity = LocalContext.current as VitalsActivity

    Row {
        BloodPressureDataCard(
            modifier = Modifier.weight(0.5f),
            icon = Icons.Default.Favorite,
            contentDescription = "heart filled in black",
            title = stringResource(id = R.string.systolic_label),
            value = activity.viewModel.vitals.value?.find { it.concept == ApplicationConstants.VitalsConceptType.SYSTOLIC_FIELD_CONCEPT }!!.value,
            measure = "mmHg"
        )
        BloodPressureDataCard(
            modifier = Modifier.weight(0.5f),
            icon = Icons.Default.FavoriteBorder,
            contentDescription = "heart outline",
            title = stringResource(id = R.string.diastolic_label),
            value = activity.viewModel.vitals.value?.find { it.concept == ApplicationConstants.VitalsConceptType.DIASTOLIC_FIELD_CONCEPT }!!.value,
            measure = "mmHg"
        )
        BloodPressureDataCard(
            modifier = Modifier.weight(0.5f),
            icon = ImageVector.vectorResource(id = R.drawable.pulse_icon),
            contentDescription = "pulse symbol",
            title = stringResource(id = R.string.pulse_label),
            value = activity.viewModel.vitals.value?.find { it.concept == ApplicationConstants.VitalsConceptType.HEART_RATE_FIELD_CONCEPT }!!.value,
            measure = "/min"
        )
    }
}

@Composable
fun NavigationButtons(navController: NavHostController) {
    var show by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxSize()
    ) {
        TextButton(onClick = { show = true}) {
            Text(
                text = "Go back",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )
        }
        TextButton(onClick = { navController.navigate(Routes.ReceiveWeightDataScreen.id) }) {
            Text(
                text = "Next",
                color = colorResource(id = R.color.allergy_orange),
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )
        }
    }
    VitalsDialog(show = show, onDismiss = {show = false}, onConfirm = {navController.popBackStack()} )
}

@Composable
fun BloodPressureDataCard(
    modifier: Modifier,
    icon: ImageVector,
    contentDescription: String,
    title: String,
    value: String,
    measure: String
) {
    return Card(
        border = BorderStroke(1.dp, Color.LightGray),
        shape = MaterialTheme.shapes.extraSmall,
        modifier = modifier.padding(3.dp),
        colors = CardColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = Color.Black,
            disabledContentColor = Color.Black
        )
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            Modifier
                .padding(10.dp)
                .size(15.dp)
        )
        Text(
            title,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
        )
        Text(
            value,
            color = Color.Black,
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
        )
        Text(
            measure,
            color = Color.Black,
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
        )
    }
}

@Preview
@Composable
fun BloodPressureDataScreenPreview() {
    val context = LocalContext.current
    BloodPressureDataScreen(navController = NavHostController(context))
}