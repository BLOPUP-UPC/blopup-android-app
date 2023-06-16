package edu.upc.openmrs.test.activities.addeditpatient

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.R
import edu.upc.blopup.AudioRecorder
import edu.upc.openmrs.activities.addeditpatient.LegalConsentDialogFragment
import edu.upc.openmrs.utilities.FileUtils
import edu.upc.openmrs.utilities.LanguageUtils
import io.mockk.*
import junit.framework.TestCase.*
import kotlinx.android.synthetic.main.fragment_patient_info.*
import kotlinx.android.synthetic.main.legal_consent.*
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.robolectric.annotation.Config

private const val ADD_EDIT_PATIENT_FRAGMENT = "edu.upc.openmrs.activities.addeditpatient.AddEditPatientFragment"

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.N])
class LegalConsentDialogFragmentTest {
    private lateinit var legalConsentScenario: FragmentScenario<LegalConsentDialogFragment>
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var context: Context


    @Before
    internal fun setup() {
        mediaPlayer = mockk(relaxUnitFun = true)
//        mockkObject(LanguageUtils)

        context = ApplicationProvider.getApplicationContext<Context>()

        mockkStatic(MediaPlayer::class)
        mockkConstructor(AudioRecorder::class)
//        mockkStatic(LanguageUtils::class)

        every { MediaPlayer.create(any(), any<Int>()) } returns mediaPlayer
//        every { LanguageUtils.getLanguageCode(any()) } returns "en"

        legalConsentScenario = launchFragment()
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    internal fun `should only enable recording button on launch`() {
        legalConsentScenario.onFragment {
            assertFalse(it.play_pause.isEnabled)
            assertFalse(it.stop.isEnabled)
            assert(it.record.isEnabled)
        }
    }

    @Test
    internal fun `should only enable playpause button when record button is clicked`() {
        legalConsentScenario.onFragment {
            it.record.performClick()
            assert(it.play_pause.isEnabled)
            assertFalse(it.record.isEnabled)
            assertFalse(it.stop.isEnabled)
        }
    }

    @Test
    internal fun `should start playing and recording when recording button is clicked`() {
        legalConsentScenario.onFragment {
            it.record.performClick()
            verify { anyConstructed<AudioRecorder>().startPlaying() }
            verify { anyConstructed<AudioRecorder>().startRecording() }
        }
    }

    @Test
    internal fun `should show recording in progress when recording button is clicked`() {
        legalConsentScenario.onFragment {
            assertFalse(it.recordingInProgress.isVisible)
            it.record.performClick()
            assert(it.recordingInProgress.isVisible)
        }
    }

    @Test
    internal fun `should disable back button while recording`() {
        legalConsentScenario.onFragment {
            assert(it.isCancelable)
            it.record.performClick()
            assertFalse(it.isCancelable)
        }
    }

    @Test
    internal fun `should change playpause icon when playpause button is clicked`() {
        val playIconAsBitmap =
            context.resources.getDrawable(R.mipmap.play_recording, null).toBitmap()
        val pauseIconAsBitmap = context.resources.getDrawable(R.mipmap.pause, null).toBitmap()
        every { mediaPlayer.isPlaying } returns false

        legalConsentScenario.onFragment {
            it.record.performClick() //isPlaying == true
            assert(pauseIconAsBitmap.sameAs(it.play_pause.background.toBitmap()))
            it.play_pause.performClick() //isPlaying == false
            assert(playIconAsBitmap.sameAs(it.play_pause.background.toBitmap()))
        }
    }

    @Test
    internal fun `should pause playing and continue recording when playpause button is clicked`() {
        every { mediaPlayer.isPlaying } returns true

        legalConsentScenario.onFragment {
            it.record.performClick()
            it.play_pause.performClick()

            verify { anyConstructed<AudioRecorder>().playPauseAudio() }
            verify(exactly = 0) { anyConstructed<AudioRecorder>().releaseRecording() }
        }
    }

    @Test
    internal fun `should stop recording when stop button is clicked`() {
        legalConsentScenario = launchFragmentInContainer()
        legalConsentScenario.onFragment {
            it.record.performClick()
            it.stop.performClick()

            verify { anyConstructed<AudioRecorder>().releaseRecording() }
        }
    }

    @Test
    internal fun `should close dialog when recording stops`() {
        legalConsentScenario.onFragment {
            it.record.performClick()
            assert(it.dialog!!.isShowing)
            it.stop.performClick()
            assertFalse(it.dialog?.isShowing!!)
        }
    }

    @Test
    @Ignore("Is entering the if statement within the onCompletionListener but the assertion is still failing")
    internal fun `should only enable stop button when audioRecorder has finished playing`() {
        legalConsentScenario.onFragment {
            it.record.performClick()

            every { anyConstructed<AudioRecorder>().hasFinishedPlaying() } returns MutableLiveData(true)

            it.onCreateView(it.layoutInflater, null, Bundle.EMPTY)

            assert(it.stop.isEnabled)
            assertFalse(it.record.isEnabled)
            assertFalse(it.play_pause.isEnabled)
        }
    }

    @Test
    @Ignore("Not able to set parentFragment in LegalConsentDialogFragment which generates a NPE")
    internal fun `should change record consent imageButton in parent when recording is completed`() {
        mockkStatic(FileUtils::class)
        every { FileUtils.fileIsCreatedSuccessfully(any()) } returns true
        val savedIconAsBitmap = context.resources.getDrawable(R.drawable.saved, null).toBitmap()

        legalConsentScenario.onFragment {

            val addEditPatient = it.parentFragmentManager.fragmentFactory.instantiate(ClassLoader.getSystemClassLoader(), ADD_EDIT_PATIENT_FRAGMENT)

            it.record.performClick()
            it.stop.performClick()
            assert(savedIconAsBitmap.sameAs(addEditPatient.record_consent_imageButton.background.toBitmap()))
        }
    }
}
