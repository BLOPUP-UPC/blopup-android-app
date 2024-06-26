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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import edu.upc.R
import edu.upc.blopup.ui.shared.components.AppDialog

@Composable
fun NavigationButtons(onClickNext: () -> Unit, onClickBack: () -> Unit) {
    var show by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxSize()
    ) {
        NavigationTextButton({ show = true }, R.string.go_back )
        NavigationTextButton({ onClickNext() }, R.string.next )
    }
    AppDialog(
        show = show,
        title = R.string.remove_vitals,
        messageDialog = R.string.cancel_vitals_dialog_message,
        onDismissText = R.string.keep_vitals_dialog_message,
        onDismiss = { show = false },
        onConfirmText = R.string.end_vitals_dialog_message,
        onConfirm = onClickBack
    )
}

@Composable
fun NavigationTextButton(onClick: () -> Unit, action: Int) {
    TextButton(onClick = { onClick() }) {
        Text(
            text = stringResource(action),
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
