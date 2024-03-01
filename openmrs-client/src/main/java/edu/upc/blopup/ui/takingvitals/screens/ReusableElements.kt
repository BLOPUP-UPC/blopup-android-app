package edu.upc.blopup.ui.takingvitals.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun VitalsDialog(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
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
                        text = stringResource(R.string.remove_vitals),
                        color = Color.White,
                        fontSize = TextUnit(20f, TextUnitType.Sp)
                    )
                }
                Column(Modifier.padding(15.dp)) {
                    Text(
                        text = stringResource(R.string.cancel_vitals_dialog_message),
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
                            Text(text = stringResource(R.string.keep_vitals_dialog_message).uppercase())
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
                            Text(text = stringResource(R.string.end_vitals_dialog_message).uppercase())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorDialog(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
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
                        text = stringResource(R.string.visit_start_error),
                        color = Color.White,
                        fontSize = TextUnit(20f, TextUnitType.Sp)
                    )
                }
                Column(Modifier.padding(15.dp)) {
                    Text(
                        text = stringResource(R.string.visit_start_error_dialog_message),
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
                            Text(text = stringResource(R.string.dialog_button_cancel).uppercase())
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
                            Text(text = stringResource(R.string.retry).uppercase())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingSpinner(modifier: Modifier) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(
            modifier = modifier,
            color = colorResource(R.color.dark_teal),
            trackColor = colorResource(R.color.grey),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VitalsDialogPreview() {
    LoadingSpinner(Modifier
        .width(70.dp)
        .padding(top = 200.dp))
}
