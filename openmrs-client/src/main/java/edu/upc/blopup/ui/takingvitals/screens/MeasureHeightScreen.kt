package edu.upc.blopup.ui.takingvitals.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.upc.R
import edu.upc.blopup.ui.shared.components.SubmitButton

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

        HeightInstructions(Modifier.weight(1f))
        HeightInput(text, onValueChange = { text = it })
        Column {
            SubmitButton(R.string.next, { onClickNext(text.text) }, isValidHeight(text.text))
        }
    }
}

@Composable
fun HeightInput(text: TextFieldValue, onValueChange: (TextFieldValue) -> Unit) {
    OutlinedTextField(value = text,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.height_value_label))},
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
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
fun HeightInstructions(modifier: Modifier) {
    Column(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.height_image),
            contentDescription = stringResource(R.string.height_image_description),
            modifier = Modifier
                .padding(25.dp)
                .align(Alignment.CenterHorizontally)
                .weight(1f, fill = false)
                .fillMaxSize(),
            contentScale = ContentScale.FillWidth
        )
        Text(
            text = stringResource(R.string.height_instructions_title),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(R.string.height_instructions),
            fontSize = 16.sp,
        )
    }}

@Preview(showSystemUi = true)
@Composable
fun HeightDataPreview() {
    MeasureHeightScreen({}, "156")
}