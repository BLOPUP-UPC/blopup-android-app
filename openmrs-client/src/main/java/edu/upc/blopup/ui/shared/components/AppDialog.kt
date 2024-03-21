package edu.upc.blopup.ui.shared.components

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import edu.upc.R

@Composable
fun AppDialog(
    show: Boolean,
    title: Int,
    messageDialog: Int,
    onDismissText: Int,
    onDismiss: () -> Unit,
    onConfirmText: Int,
    onConfirm: () -> Unit
) {
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
                        fontSize = TextUnit(20f, TextUnitType.Sp)
                    )
                }
                Column(Modifier.padding(15.dp)) {
                    Text(
                        text = stringResource(messageDialog),
                        color = Color.Gray
                    )
                }
                Column(Modifier.padding(start = 25.dp)) {
                    Row {
                        Button(
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp),
                            onClick = { onDismiss() },
                            shape = MaterialTheme.shapes.extraSmall,
                            contentPadding = PaddingValues(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(
                                    R.color.dark_grey_for_stroke
                                )
                            )
                        ) {
                            Text(text = stringResource(onDismissText).uppercase())
                        }
                        Button(
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 10.dp),
                            onClick = { onConfirm() },
                            shape = MaterialTheme.shapes.extraSmall,
                            contentPadding = PaddingValues(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(
                                    R.color.allergy_orange
                                )
                            )
                        ) {
                            Text(text = stringResource(onConfirmText).uppercase())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnBackPressButtonConfirmDialog(onClickBack: () -> Unit) {
    var showAlertDialog by remember { mutableStateOf(false) }
    BackHandler {
        showAlertDialog = true
    }

    if (showAlertDialog) {
        AppDialog(
            show = true,
            onDismiss = { showAlertDialog = false },
            onConfirm = { showAlertDialog = false; onClickBack() },
            title = R.string.remove_vitals,
            messageDialog = R.string.cancel_vitals_dialog_message,
            onDismissText = R.string.keep_vitals_dialog_message,
            onConfirmText = R.string.end_vitals_dialog_message
        )
    }
}

@Preview
@Composable
fun RemoveVitalsDialogPreview() {
    AppDialog(
        show = true,
        onDismiss = {},
        onConfirm = {},
        title = R.string.remove_vitals,
        messageDialog = R.string.cancel_vitals_dialog_message,
        onDismissText = R.string.keep_vitals_dialog_message,
        onConfirmText = R.string.end_vitals_dialog_message
    )
}
