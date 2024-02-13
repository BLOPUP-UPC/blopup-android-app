package edu.upc.blopup.ui.takingvitals.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ReceiveWeightDataScreen(navigationController: NavHostController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp)
    ) {
        Text(
            text = "Receive Weight Data Screen coming soon...",
            fontSize = TextUnit(38f, TextUnitType.Sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 30.dp)
        )
        Button(onClick = { navigationController.popBackStack() }) {
            Text(text = "Press me to go back!")
        }
    }
}

@Preview
@Composable
fun WeightDataPreview() {
    ReceiveWeightDataScreen(navigationController = NavHostController(LocalContext.current))
}
