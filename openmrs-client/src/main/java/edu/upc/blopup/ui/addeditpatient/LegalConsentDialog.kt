package edu.upc.blopup.ui.addeditpatient

import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import edu.upc.R
import edu.upc.blopup.ui.shared.components.SubmitButton

@Composable
fun LegalConsentDialog(languageSelected: String, onCloseDialog: () -> Unit) {
    var isRecordingInProcess by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = { onCloseDialog() }) {
        Column(
            Modifier
                .background(Color.White)
        ) {
            if (isRecordingInProcess) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(25.dp)
                ) {
                    Image(
                        painterResource(R.drawable.recording_in_progress),
                        contentDescription = "recording icon",
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .size(30.dp),
                        colorFilter = ColorFilter.tint(colorResource(R.color.allergy_orange))
                    )
                    Text(
                        text = stringResource(R.string.recording_in_progress),
                        color = colorResource(R.color.allergy_orange),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .weight(1f)
            ) {
                Text(text = stringResource(R.string.legal_consent_intro))
                Text(text = stringResource(R.string.legal_consent))
                BulletPointText(text = R.string.first_bullet_point)
                BulletPointText(text = R.string.second_bullet_point)
                BulletPointText(text = R.string.third_bullet_point)
                BulletPointText(text = R.string.fourth_bullet_point)
                Text(text = stringResource(R.string.bottom_text))
            }
            if (!isRecordingInProcess) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    SubmitButton(
                        title = R.string.record_legal_consent,
                        onClickNext = { isRecordingInProcess = true },
                        enabled = true
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.pause).uppercase(),
                    color = colorResource(R.color.allergy_orange),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 15.dp, horizontal = 20.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray)
                ) {
                    Text(
                        text = stringResource(R.string.stop_save),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier
                            .padding(vertical = 15.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun BulletPointText(text: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 10.dp)
    ) {
        AndroidView(factory = { context ->
            ImageView(context).apply {
                setImageResource(R.drawable.circle)
            }
        }, modifier = Modifier.padding(end = 15.dp))
        Text(text = stringResource(text))
    }
}

@Preview
@Composable
fun LegalConsentDialogPreview() {
    LegalConsentDialog("en") { }
}