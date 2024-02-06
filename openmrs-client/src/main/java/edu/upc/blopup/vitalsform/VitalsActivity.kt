package edu.upc.blopup.vitalsform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.upc.R

class VitalsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BloodPressureInstructions()
        }
    }
}

@Composable
fun BloodPressureInstructions() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Box(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Image(
                painter = painterResource(R.drawable.how_to_measure),
                contentDescription = stringResource(R.string.bp_image_description),
            )
        }
        Column {
            Text(text = stringResource(R.string.blood_pressure_instructions_title), style = TextStyle(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 8.dp))
            Text(text = stringResource(R.string.blood_pressure_instructions_two))
        }
        InfoButton()
        ReceiveBloodPressureDataButton()
    }
}

@Composable
fun InfoButton() {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
        .fillMaxWidth()
        .clickable { TODO() }) {
        Row {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.icon_bp_info),
                contentDescription = "Info",
                modifier = Modifier.padding(end = 8.dp),
                tint = colorResource(R.color.allergy_orange)
            )
            Text(text = stringResource(R.string.how_to_activate_the_device_label))
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
    Button(shape = MaterialTheme.shapes.extraSmall, onClick = { /*TODO*/ }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.bluetooth_blue))) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource( R.string.blood_pressure_button_label), style = TextStyle(fontWeight = FontWeight.Bold))
            Icon(painter = painterResource(id = android.R.drawable.stat_sys_data_bluetooth), contentDescription = "Bluetooth icon")
        }

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BloodPressureInstructions()
}