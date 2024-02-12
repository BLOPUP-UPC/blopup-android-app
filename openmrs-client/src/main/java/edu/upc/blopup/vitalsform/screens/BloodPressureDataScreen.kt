package edu.upc.blopup.vitalsform.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import edu.upc.R
import edu.upc.blopup.vitalsform.model.Routes

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
private fun NavigationButtons(navController: NavHostController) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxSize()
    ) {
        TextButton(onClick = { navController.popBackStack() }) {
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
}

@Composable
fun BloodPressureValues() {
    Row() {
        Card(
            border = BorderStroke(1.dp, Color.LightGray),
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier
                .padding(5.dp)
                .weight(0.5f),
            colors = CardColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                contentColor = Color.Black,
                disabledContentColor = Color.Black
            )
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = "Black heart",
                modifier = Modifier
                    .padding(10.dp)
                    .size(15.dp)
            )
            Text(
                "Systolic",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp)
            )
            Text(
                "161",
                color = Color.Black,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 10.dp, end = 10.dp)

            )
            Text(
                "mmHg",
                color = Color.Black,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            )
        }
        Card(
            border = BorderStroke(1.dp, Color.LightGray),
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier
                .padding(5.dp)
                .weight(0.5f),
            colors = CardColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                contentColor = Color.Black,
                disabledContentColor = Color.Black
            )

        ) {
            Icon(
                Icons.Default.FavoriteBorder,
                contentDescription = "Heart outline",
                modifier = Modifier
                    .padding(10.dp)
                    .size(15.dp)
            )
            Text(
                "Diastolic",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp)
            )
            Text(
                "84",
                color = Color.Black,
                style = TextStyle(fontSize = 24.sp),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp)
            )
            Text(
                "mmHg",
                color = Color.Black,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            )
        }
        Card(
            border = BorderStroke(1.dp, Color.LightGray),
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier
                .padding(5.dp)
                .weight(0.5f),
            colors = CardColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                contentColor = Color.Black,
                disabledContentColor = Color.Black
            )
        ) {
            Icon(
                painterResource(id = R.drawable.pulse_icon),
                contentDescription = "Pulse symbol",
                Modifier
                    .padding(10.dp)
                    .size(15.dp)
            )
            Text(
                "Pulse",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp)
            )
            Text(
                "82",
                color = Color.Black,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 10.dp, end = 10.dp)
            )
            Text(
                "bpm",
                color = Color.Black,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            )
        }
    }
}


@Preview
@Composable
fun BloodPressureDataScreenPreview() {
    val context = LocalContext.current
    BloodPressureDataScreen(navController = NavHostController(context))
}