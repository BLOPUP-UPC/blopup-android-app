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
import androidx.compose.ui.Alignment
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
                    .padding(bottom = 8.dp)
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
                    Text(
                        text = stringResource(messageDialog),
                        color = Color.Gray
                    )
                }
                Column(Modifier.align(Alignment.End)) {
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
