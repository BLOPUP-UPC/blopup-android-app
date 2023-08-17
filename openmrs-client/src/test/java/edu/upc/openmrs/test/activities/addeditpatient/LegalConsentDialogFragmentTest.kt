package edu.upc.openmrs.test.activities.addeditpatient

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
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
import junit.framework.TestCase.assertFalse
import kotlinx.android.synthetic.main.fragment_patient_info.*
import kotlinx.android.synthetic.main.legal_consent.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith


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

    @Ignore
    @Test
    internal fun `should hide recording and show pause and stop buttons when recording is clicked`() {
        legalConsentScenario.onFragment {
            it.record.performClick()
            assert(it.play_pause.isVisible)
            assert(it.stop.isVisible)
            assertFalse(it.record.isVisible)
        }
    }

    @Ignore
    @Test
    internal fun `should only display recording button on launch`() {
        legalConsentScenario.onFragment {
            assertFalse(it.play_pause.isVisible)
            assertFalse(it.stop.isVisible)
            assert(it.record.isVisible)
        }
    }

    @Ignore
    @Test
    internal fun `playpause button should be enabled and stop button should be disabled when record is clicked`() {
        legalConsentScenario.onFragment {
            it.record.performClick()
            assert(it.play_pause.isEnabled)
            assertFalse(it.stop.isEnabled)
        }
    }

    @Ignore
    @Test
    internal fun `should start playing and recording when recording button is clicked`() {
        legalConsentScenario.onFragment {
            it.record.performClick()
            verify { anyConstructed<AudioRecorder>().startPlaying() }
            verify { anyConstructed<AudioRecorder>().startRecording() }
        }
    }

    @Ignore
    @Test
    internal fun `should show recording in progress when recording button is clicked`() {
        legalConsentScenario.onFragment {
            assertFalse(it.recordingInProgress.isVisible)
            it.record.performClick()
            assert(it.recordingInProgress.isVisible)
        }
    }

    @Ignore
    @Test
    internal fun `should disable back button while recording`() {
        legalConsentScenario.onFragment {
            assert(it.isCancelable)
            it.record.performClick()
            assertFalse(it.isCancelable)
        }
    }

    @Ignore
    @Test
    internal fun `should change playpause text when playpause button is clicked`() {
        every { mediaPlayer.isPlaying } returns false

        legalConsentScenario.onFragment {
            it.record.performClick() //isPlaying == true
            assert(it.play_pause.text == context.getString(R.string.pause))
            it.play_pause.performClick() //isPlaying == false
            assert(it.play_pause.text == context.getString(R.string.resume))
        }
    }

    @Ignore
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

    @Ignore
    @Test
    internal fun `should stop recording when stop button is clicked`() {
        legalConsentScenario = launchFragmentInContainer()
        legalConsentScenario.onFragment {
            it.record.performClick()
            it.stop.performClick()

            verify { anyConstructed<AudioRecorder>().releaseRecording() }
        }
    }

    @Ignore
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
    internal fun `should enable and change color for stop button when audioRecorder has finished playing`() {
        legalConsentScenario.onFragment {
            it.record.performClick()

            every { anyConstructed<AudioRecorder>().hasFinishedPlaying() } returns MutableLiveData(
                true
            )

            it.onCreateView(it.layoutInflater, null, Bundle.EMPTY)

            assert(it.stop.isEnabled)
            assert(it.stop.background.equals(R.color.color_accent))
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
