package edu.upc.blopup.ui.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import edu.upc.R

@Composable
fun ErrorDialog(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit, title:Int, instructions: Int? = null) {
    if (show) {
        Dialog(onDismissRequest = { onDismiss() }) {
            Column(
                Modifier
                    .background(Color.White)
            ) {
                Column(
                    Modifier
                        .background(colorResource(id = R.color.dark_teal))
                        .fillMaxWidth()
                        .padding(15.dp)
                ) {
                    Text(
                        text = stringResource(title),
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }
                Column(Modifier.padding(15.dp)) {
                    val instructionsText = instructions?.let { "\n${stringResource(it)}" } ?: ""
                    Text(
                        text = stringResource(R.string.visit_start_error_dialog_message) + instructionsText,
                        color = Color.Gray
                    )
                }
                Column(Modifier.padding(start = 25.dp)) {
                    Row {
                        ActionButton(onDismiss, R.color.dark_grey_for_stroke, R.string.dialog_button_cancel)
                        ActionButton(onConfirm, R.color.allergy_orange, R.string.retry)
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(onClickEvent: () -> Unit, color: Int, action: Int) {
    Button(
        modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp),
        onClick = { onClickEvent() },
        shape = MaterialTheme.shapes.extraSmall,
        contentPadding = PaddingValues(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(
                color
            )
        )
    ) {
        Text(text = stringResource(action).uppercase())
    }}

@Preview
@Composable
fun ErrorDialogPreview() {
    ErrorDialog(true, {}, {}, R.string.bluetooth_error_connection, R.string.bluetooth_error_instructions_scale)
}