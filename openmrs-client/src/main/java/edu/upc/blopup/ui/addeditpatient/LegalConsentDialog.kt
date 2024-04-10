package edu.upc.blopup.ui.addeditpatient

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
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
import androidx.compose.ui.text.font.FontWeight
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
import edu.upc.openmrs.activities.addeditpatient.LegalConsentDialogFragment
import edu.upc.openmrs.utilities.FileUtils
import edu.upc.openmrs.utilities.FileUtils.fileIsCreatedSuccessfully
import java.util.Locale

@Composable
fun LegalConsentDialog(
    languageSelected: String,
    onCloseDialog: () -> Unit,
    context: Context,
    onSaveLegalConsent: (String) -> Unit
) {

    var isRecordingInProcess by remember { mutableStateOf(false) }
    var isAudioBeingPlayed by remember { mutableStateOf(false) }
    val languageCode = getLanguageCode(languageSelected, context)
    lateinit var fileName: String
    var isResumed by remember { mutableStateOf(true) }

    FileUtils.getRecordingFilePath().also { fileName = it }
    val inputFileId =
        if (BuildConfig.DEBUG) FileUtils.getFileByLanguage(
            context as Activity?,
            LegalConsentDialogFragment.TAG,
            "test"
        ) else
            FileUtils.getFileByLanguage(
                context as Activity?,
                LegalConsentDialogFragment.TAG,
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

    Dialog(onDismissRequest = { onCloseDialog() }) {
        Column(
            Modifier
                .background(Color.White)
                .padding(top = 30.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
        ) {
            if (isRecordingInProcess) {
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
                }
            }
            Column(
                verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .weight(1f)
            ) {
                Text(text = stringResource(R.string.legal_consent_intro))
                Text(text = getTextInLanguageSelected(languageCode!!, R.string.legal_consent, context))
                BulletPointText(text = R.string.first_bullet_point, languageCode, context)
                BulletPointText(text = R.string.second_bullet_point, languageCode = languageCode, context = context)
                BulletPointText(text = R.string.third_bullet_point, languageCode = languageCode, context = context)
                BulletPointText(text = R.string.fourth_bullet_point, languageCode = languageCode, context = context)
                Text(text = getTextInLanguageSelected(languageCode, R.string.bottom_text, context))
            }
            if (!isRecordingInProcess) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    SubmitButton(
                        title = R.string.record_legal_consent,
                        onClickNext = {
                            isRecordingInProcess =
                                true; audioRecorder.startRecording(); audioRecorder.startPlaying()
                        },
                        enabled = true
                    )
                }
            } else {
                Text(
                    text = if (isResumed) stringResource(R.string.resume).uppercase() else stringResource(
                        R.string.pause
                    ).uppercase(),
                    color = colorResource(R.color.allergy_orange),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(vertical = 15.dp, horizontal = 20.dp)
                        .clickable { audioRecorder.playPauseAudio(); isResumed = !isResumed }
                )
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
fun BulletPointText(text: Int, languageCode: String, context: Context) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 10.dp)
    ) {
        AndroidView(factory = { context ->
            ImageView(context).apply {
                setImageResource(R.drawable.circle)
            }
        }, modifier = Modifier.padding(end = 15.dp))
        Text(text = getTextInLanguageSelected(languageCode, text, context))
    }
}

private fun getTextInLanguageSelected(
    selectedLanguage: String,
    resourceId: Int,
    context: Context
): String {
    val requestedLocale = Locale(selectedLanguage)
    val config =
        Configuration(context.resources.configuration).apply { setLocale(requestedLocale) }

    return context.createConfigurationContext(config).getText(resourceId).toString()
}

private fun getLanguageCode(language: String?, context: Context): String? {
    val currentLocale = Locale.getDefault()
    val languageMap = mapOf(
        context.getString(R.string.english) to "en",
        context.getString(R.string.spanish) to "es",
        context.getString(R.string.catalan) to "ca",
        context.getString(R.string.italian) to "it",
        context.getString(R.string.portuguese) to "pt",
        context.getString(R.string.german) to "de",
        context.getString(R.string.french) to "fr",
        context.getString(R.string.moroccan) to "ar",
        context.getString(R.string.russian) to "ru",
        context.getString(R.string.ukrainian) to "uk",
    )
    return languageMap[language ?: "English"]?.lowercase(currentLocale)
}

@Preview
@Composable
fun LegalConsentDialogPreview() {
    LegalConsentDialog("en", {}, LocalContext.current, {})
}