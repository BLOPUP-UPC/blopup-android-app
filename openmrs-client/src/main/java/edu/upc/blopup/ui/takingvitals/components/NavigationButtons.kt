package edu.upc.blopup.ui.takingvitals.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import edu.upc.R

@Composable
fun NavigationButtons(onClickNext: () -> Unit, onClickBack: () -> Unit) {
    var show by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxSize()
    ) {
        TextButton(onClick = { show = true }) {
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
        show = show,
        onDismiss = { show = false },
        onConfirm = onClickBack
    )
}