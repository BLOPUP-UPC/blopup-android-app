package edu.upc.blopup.ui.takingvitals.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
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
            fontSize = TextUnit(20F, TextUnitType.Sp),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
fun OnBackPressButtonConfirmDialog(onClickBack: () -> Unit){
    var showAlertDialog by remember { mutableStateOf(false)}
    BackHandler{
        showAlertDialog = true
    }

    if(showAlertDialog){
        VitalsDialog(show = true, onDismiss = { showAlertDialog = false },
            onConfirm = { showAlertDialog = false ; onClickBack() })
    }
}

@Preview
@Composable
fun DataReceivedSuccessfullyPreview() {
    DataReceivedSuccessfully()
}

