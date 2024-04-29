package edu.upc.blopup.ui.createpatient.components

import android.app.Activity
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Observer
import edu.upc.BuildConfig
import edu.upc.R
import edu.upc.blopup.AudioRecorder
import edu.upc.blopup.ui.shared.components.SubmitButton
import edu.upc.openmrs.utilities.FileUtils
import edu.upc.openmrs.utilities.FileUtils.fileIsCreatedSuccessfully
import java.util.Locale

@Composable
fun LegalConsentDialog(
    languageSelected: String,
    onCloseDialog: () -> Unit,
    onSaveLegalConsent: (String) -> Unit,
    legalConsentFile: String,
    getStringByResourceId: (Int) -> String,
    getTextInLanguageSelected: (String, Int) -> String
) {

    val context = LocalContext.current

    var isRecordingInProcess by remember { mutableStateOf(false) }
    var isAudioBeingPlayed by remember { mutableStateOf(false) }
    val languageCode = getLanguageCode(languageSelected, getStringByResourceId)
    lateinit var fileName: String
    var isResumed by remember { mutableStateOf(true) }

    FileUtils.getRecordingFilePath().also { fileName = it }
    val inputFileId =
        if (BuildConfig.DEBUG) FileUtils.getFileByLanguage(
            context as Activity?,
            "legal_consent",
            "test"
        ) else
            FileUtils.getFileByLanguage(
                context as Activity?,
                "legal_consent",
                languageCode
            )

    val audioRecorder = remember {
        AudioRecorder(
            fileName,
            context,
            inputFileId,
        )
    }

    DisposableEffect(audioRecorder) {
        val observer = Observer<Boolean> { hasFinished ->
            isAudioBeingPlayed = !hasFinished
        }
        val liveData = audioRecorder.hasFinishedPlaying()
        liveData.observeForever(observer)

        onDispose {
            liveData.removeObserver(observer)
        }
    }

    Dialog(onDismissRequest = { if(!isRecordingInProcess) onCloseDialog() }) {
        Column(
            Modifier
                .background(Color.White)
                .padding(top = 30.dp)
        ) {
            if (isRecordingInProcess) { ShowRecordingInProgress() }

            LegalConsentWording(languageCode, Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).weight(1f), getTextInLanguageSelected)

            if (!isRecordingInProcess) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    SubmitButton(
                        title = R.string.record_legal_consent,
                        onClickNext = {
                            isRecordingInProcess = true
                            if(legalConsentFile.isNotEmpty()) {
                                FileUtils.removeLocalRecordingFile(legalConsentFile)
                            }
                            audioRecorder.startRecording()
                            audioRecorder.startPlaying()
                        },
                        enabled = true
                    )
                }
            } else {
                if (isAudioBeingPlayed) {
                    Text(
                        text = if (isResumed) stringResource(R.string.pause).uppercase() else stringResource(
                            R.string.resume
                        ).uppercase(),
                        color = colorResource(R.color.allergy_orange),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(vertical = 15.dp, horizontal = 20.dp)
                            .clickable { audioRecorder.playPauseAudio(); isResumed = audioRecorder.isPlaying() }
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isAudioBeingPlayed) Color.LightGray else colorResource(R.color.allergy_orange))
                        .clickable(enabled = !isAudioBeingPlayed) {
                            if (!isAudioBeingPlayed) {
                                audioRecorder.releaseRecording()
                                onCloseDialog()
                                if (fileIsCreatedSuccessfully(fileName)) {
                                    onSaveLegalConsent(fileName)
                                }
                            }
                        }
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
fun ShowRecordingInProgress() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 25.dp, start = 20.dp, end = 20.dp)
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
    }}

@Composable
fun LegalConsentWording(
    languageCode: String?,
    modifier: Modifier,
    getTextInLanguageSelected: (String, Int) -> String
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween, modifier = modifier
    ) {
        Text(text = stringResource(R.string.legal_consent_intro_first), modifier = Modifier.padding(bottom = 15.dp))
        Text(text = legalConsentFormattedText())
        Text(text = legalConsentFormattedTextSecond(), modifier = Modifier.padding(bottom = 15.dp))
        Text(text = getTextInLanguageSelected(languageCode!!, R.string.legal_consent))
        BulletPointText(R.string.first_bullet_point, languageCode, getTextInLanguageSelected)
        BulletPointText(R.string.second_bullet_point, languageCode, getTextInLanguageSelected)
        BulletPointText(R.string.third_bullet_point, languageCode, getTextInLanguageSelected)
        BulletPointText(R.string.fourth_bullet_point, languageCode, getTextInLanguageSelected)
        Text(text = getTextInLanguageSelected(languageCode, R.string.bottom_text))
    }
}

@Composable
private fun legalConsentFormattedText() = buildAnnotatedString {
    append(stringResource(R.string.legal_consent_intro_second))
    append(" ")

    withStyle(
        SpanStyle(
            fontWeight = FontWeight.Bold,
        )
    ) {
        append(stringResource(R.string.to_pause))
    }
    append(" ")
    append(stringResource(R.string.legal_consent_intro_third))
}

@Composable
private fun legalConsentFormattedTextSecond() = buildAnnotatedString {
    append(stringResource(R.string.legal_consent_intro_fourth))
    append(" ")
    withStyle(
        SpanStyle(
            fontWeight = FontWeight.Bold,
        )
    ) {
        append(stringResource(R.string.stop_and_save))
    }
    append(" ")
    append(stringResource(R.string.legal_consent_intro_fifth))
}



@Composable
fun BulletPointText(
    text: Int,
    languageCode: String,
    getTextInLanguageSelected: (String, Int) -> String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 10.dp)
    ) {
        AndroidView(factory = { context ->
            ImageView(context).apply {
                setImageResource(R.drawable.circle)
            }
        }, modifier = Modifier.padding(end = 15.dp))
        Text(text = getTextInLanguageSelected(languageCode, text))
    }
}

private fun getLanguageCode(
    language: String?,
    getStringByResourceId: (Int) -> String
): String? {
    val currentLocale = Locale.getDefault()
    val languageMap = mapOf(
        getStringByResourceId(R.string.english) to "en",
        getStringByResourceId(R.string.spanish) to "es",
        getStringByResourceId(R.string.catalan) to "ca",
        getStringByResourceId(R.string.italian) to "it",
        getStringByResourceId(R.string.portuguese) to "pt",
        getStringByResourceId(R.string.german) to "de",
        getStringByResourceId(R.string.french) to "fr",
        getStringByResourceId(R.string.moroccan) to "ar",
        getStringByResourceId(R.string.russian) to "ru",
        getStringByResourceId(R.string.ukrainian) to "uk",
    )
    return languageMap[language ?: "English"]?.lowercase(currentLocale)
}

@Preview
@Composable
fun LegalConsentDialogPreview() {
    LegalConsentDialog("en", {}, {}, "", { _ -> ""}, { _, _ -> "" })
}