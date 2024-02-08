package edu.upc.blopup.vitalsform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class VitalsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BloodPressureScreenWithAppBar()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VitalsActivityPreview() {
    BloodPressureScreenWithAppBar()
}