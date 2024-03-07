package edu.upc.blopup.ui.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import edu.upc.R

@Composable
fun SubmitButton(title: Int, onClickNext: () -> Unit, enabled: Boolean) {
    Button(
        enabled = enabled,
        shape = MaterialTheme.shapes.extraSmall,
        onClick = {onClickNext() },
        contentPadding = PaddingValues(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(
                R.color.allergy_orange
            )
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = title),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(16f, TextUnitType.Sp),
                )
            )
        }
    }
}

@Preview
@Composable
fun OrangeButtonPreview() {
    SubmitButton(
        R.string.finalise_treatment,
        {},
        true
    )
}
