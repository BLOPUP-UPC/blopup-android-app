package edu.upc.blopup.ui.shared.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.upc.R

@Composable
fun LoadingSpinner(modifier: Modifier, color: Int = R.color.dark_teal) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(
            modifier = modifier,
            color = colorResource(color),
            trackColor = colorResource(R.color.grey),
        )
    }
}

@Preview
@Composable
fun LoadingSpinnerPreview() {
    LoadingSpinner(Modifier.padding(16.dp))
}