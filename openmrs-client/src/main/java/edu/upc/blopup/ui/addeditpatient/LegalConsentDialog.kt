package edu.upc.blopup.ui.addeditpatient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun LegalConsentDialog(languageSelected: String, onCloseDialog: () -> Unit) {
    Dialog(onDismissRequest = { onCloseDialog() }) {
        Column(
            Modifier
                .background(Color.White)
                .padding(10.dp)
        ) {
            Text(text = "Legal Consent Dialog in $languageSelected")
        }
    }
}

@Preview
@Composable
fun LegalConsentDialogPreview() {
    LegalConsentDialog("en") { }
}