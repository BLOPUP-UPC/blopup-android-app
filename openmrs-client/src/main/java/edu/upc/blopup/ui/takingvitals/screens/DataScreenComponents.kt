package edu.upc.blopup.ui.takingvitals.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
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
            "Data Received Successfully.",
            fontSize = TextUnit(20F, TextUnitType.Sp),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
fun VitalsDataCard(
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
                .padding(8.dp)
                .size(16.dp)
        )
        Text(
            title,
            color = Color.Black,
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
        )
        Text(
            value,
            color = Color.Black,
            style = TextStyle(fontSize = 32.sp),
            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
        )
        Text(
            measure,
            color = Color.Black,
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
        )
    }
}

@Composable
fun NavigationButtons(
    onClickNext: () -> Unit,
    onClickBack: () -> Unit,
    showDialog: Boolean,
    onShowDialogChange: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxSize()
    ) {
        TextButton(onClick = { onShowDialogChange(true) }) {
            Text(
                text = stringResource(R.string.go_back),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )
        }
        TextButton(onClick = onClickNext) {
            Text(
                text = stringResource(R.string.next),
                color = colorResource(id = R.color.allergy_orange),
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )
        }
    }
    VitalsDialog(
        show = showDialog,
        onDismiss = { onShowDialogChange(false) },
        onConfirm = { onShowDialogChange(false); onClickBack() }
    )
}