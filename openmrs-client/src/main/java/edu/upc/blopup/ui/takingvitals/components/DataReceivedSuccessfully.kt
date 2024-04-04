package edu.upc.blopup.ui.takingvitals.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.upc.R

@Composable
fun DataReceivedSuccessfully() {
    Column(
        modifier = Modifier.padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.circle_green_checkmark),
            contentDescription = "Large white tick in a green circle."
        )
        Text(
            stringResource(R.string.data_received_successfully),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Preview
@Composable
fun DataReceivedSuccessfullyPreview() {
    DataReceivedSuccessfully()
}

