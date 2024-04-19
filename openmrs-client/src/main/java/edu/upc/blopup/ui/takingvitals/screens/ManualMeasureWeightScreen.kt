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
fun ManualMeasureWeightScreen(
    onClickNext: (String) -> Unit,
) {
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
        var text by remember { mutableStateOf(TextFieldValue("")) }
        
        ManualWeightInstructions(Modifier)
        WeightInput(text, onValueChange = { text = it })
        Column {
            SubmitButton(R.string.next, { onClickNext(text.text) }, isValidWeight(text.text))
        }
    }
}

@Composable
fun WeightInput(text: TextFieldValue, onValueChange: (TextFieldValue) -> Unit) {
    OutlinedTextField(
        value = text,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.weight_value_label)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        suffix = {
            Text(
                text = stringResource(id = R.string.weight_unit)
            )
        },
        isError = !isValidHeight(text.text),
        supportingText = { if (!isValidWeight(text.text)) Text(text = stringResource(R.string.weight_range)) },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Black,
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            focusedLabelColor = Color.Black,
            errorContainerColor = Color.Transparent
        )

    )
}

fun isValidWeight(text: String): Boolean {
    val number = text.toIntOrNull() ?: return false
    return number in 30..280
}

@Composable
fun ManualWeightInstructions(modifier: Modifier) {
    Column(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.scale_image),
            contentDescription = stringResource(R.string.weight_image_description),
            modifier = Modifier
                .padding(30.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
        Text(
            text = stringResource(R.string.weight_instructions_title),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun ManualMeasureWeightScreenDataPreview() {
    ManualMeasureWeightScreen({})
}