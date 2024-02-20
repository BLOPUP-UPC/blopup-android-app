package edu.upc.blopup.ui.takingvitals.screens

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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import edu.upc.R

@Composable
fun VitalsDialog(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    if(show){
        Dialog(onDismissRequest = { onDismiss() }) {
            Column(
                Modifier
                    .background(Color.White)
            ) {
                Column(
                    Modifier
                        .background(colorResource(id = R.color.dark_teal))
                        .fillMaxWidth()
                        .padding(15.dp)) {
                    Text(text = stringResource(R.string.remove_vitals), color = Color.White, fontSize = TextUnit(20f, TextUnitType.Sp))
                }
                Column(Modifier.padding(15.dp)) {
                    Text(text = stringResource(R.string.cancel_vitals_dialog_message), color = Color.Gray)
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