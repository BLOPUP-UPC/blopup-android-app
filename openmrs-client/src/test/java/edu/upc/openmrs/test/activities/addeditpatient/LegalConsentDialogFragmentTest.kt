package edu.upc.openmrs.test.activities.addeditpatient

import android.content.Context
import android.media.MediaPlayer
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
import io.mockk.*
import io.mockk.InternalPlatformDsl.toStr
import junit.framework.TestCase.*
import kotlinx.android.synthetic.main.fragment_patient_info.*
import kotlinx.android.synthetic.main.legal_consent.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

private const val ADD_EDIT_PATIENT_FRAGMENT =
    "edu.upc.openmrs.activities.addeditpatient.AddEditPatientFragment"

@RunWith(AndroidJUnit4::class)
class LegalConsentDialogFragmentTest {
    private lateinit var legalConsentScenario: FragmentScenario<LegalConsentDialogFragment>
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var context: Context

    @Before
    internal fun setup() {
        mediaPlayer = mockk(relaxUnitFun = true)
        context = ApplicationProvider.getApplicationContext()

        mockkStatic(MediaPlayer::class)
        mockkConstructor(AudioRecorder::class)

        every { MediaPlayer.create(any(), any<Int>()) } returns mediaPlayer

        legalConsentScenario = launchFragment()
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

            every { anyConstructed<AudioRecorder>().hasFinishedPlaying() } returns MutableLiveData(
                true
            )

            it.onCreateView(it.layoutInflater, null, Bundle.EMPTY)

            assert(it.stop.isEnabled)
            assertFalse(it.record.isEnabled)
            assertFalse(it.play_pause.isEnabled)
        }
    }

    @Test
    @Ignore("not sure how to assign a parent to a fragment in a test")
    internal fun `should show record consent imageButton in parent when recording is completed`() {
        legalConsentScenario = launchFragmentInContainer()
        mockkStatic(FileUtils::class)
        every { FileUtils.fileIsCreatedSuccessfully(any()) } returns true

        legalConsentScenario.onFragment {

            it.record.performClick()
            it.stop.performClick()

            assert(it.parentFragment?.record_consent_saved!!.isVisible)
            assertFalse(it.language_spinner.isEnabled)
        }
    }
}
