package edu.upc.blopup.ui.shared.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import edu.upc.R

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