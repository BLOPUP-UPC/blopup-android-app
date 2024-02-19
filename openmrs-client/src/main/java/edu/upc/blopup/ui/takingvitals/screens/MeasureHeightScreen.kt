package edu.upc.blopup.ui.takingvitals.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import edu.upc.R

@Composable
fun MeasureHeightScreen(onClickNext: (String) -> Unit, startingHeight : String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        var text by remember { mutableStateOf(TextFieldValue(startingHeight.trim())) }

        HeightInstructions()
        HeightInput(text, onValueChange = { text = it })
        Column {
            NextButton({ onClickNext(text.text) }, isValidHeight(text.text))
        }
    }
}

@Composable
fun HeightInput(text: TextFieldValue, onValueChange: (TextFieldValue) -> Unit) {
    OutlinedTextField(value = text,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.height_value_label))},
        modifier = Modifier.fillMaxWidth(),
        suffix = {
            Text(
                text = stringResource(id = R.string.height_unit)
            )
        },
        isError = !isValidHeight(text.text),
        supportingText = { if (!isValidHeight(text.text)) Text(text = stringResource(R.string.height_range)) },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Black,
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            focusedLabelColor = Color.Black,
            errorContainerColor = Color.Transparent
            )

    )
}

fun isValidHeight(text: String): Boolean {
    val number = text.toIntOrNull() ?: return false
    return number in 50..280
}

@Composable
fun HeightInstructions() {
    Column {
        Image(
            painter = painterResource(R.drawable.height_image),
            contentDescription = stringResource(R.string.height_image_description),
            modifier = Modifier
                .padding(30.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
        Text(
            text = stringResource(R.string.height_instructions_title),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(20f, TextUnitType.Sp)
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(R.string.height_instructions),
            fontSize = TextUnit(16f, TextUnitType.Sp),
        )
    }}


@Composable
fun NextButton(onClickNext: () -> Unit, enabled: Boolean) {
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
                text = "Next",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(16f, TextUnitType.Sp),
                )
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun HeightDataPreview() {
    MeasureHeightScreen({}, "156")
}