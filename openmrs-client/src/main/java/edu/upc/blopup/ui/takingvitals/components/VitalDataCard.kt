package edu.upc.blopup.ui.takingvitals.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.upc.R

@Composable
fun VitalDataCard(
    modifier: Modifier,
    icon: ImageVector,
    contentDescription: String,
    title: String,
    value: String,
    measure: String
) {
    return Card(
        border = BorderStroke(1.dp, Color.LightGray),
        shape = MaterialTheme.shapes.extraSmall,
        modifier = modifier.padding(3.dp),
        colors = CardColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = Color.Black,
            disabledContentColor = Color.Black
        )
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            Modifier
                .padding(8.dp)
                .size(16.dp)
        )
        Text(
            title,
            color = Color.Black,
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Text(
            value,
            color = Color.Black,
            style = TextStyle(fontSize = 32.sp),
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Text(
            measure,
            color = Color.Black,
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
        )
    }
}

@Preview
@Composable
fun VitalDataCardPreview() {
    VitalDataCard(
        modifier = Modifier,
        icon = Icons.Default.Favorite,
        contentDescription = "heart filled in black",
        title = stringResource(id = R.string.systolic_label),
        value = "120",
        measure = "mmHg"
    )
}