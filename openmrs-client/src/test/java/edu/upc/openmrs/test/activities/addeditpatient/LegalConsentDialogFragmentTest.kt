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
            with(it.legalConsentBinding) {
                record.performClick()
                assert(playPause.isVisible)
                assert(stop.isVisible)
                assertFalse(record.isVisible)
            }
        }
    }

    @Ignore
    @Test
    internal fun `should only display recording button on launch`() {
        legalConsentScenario.onFragment {
            assertFalse(it.legalConsentBinding.playPause.isVisible)
            assertFalse(it.legalConsentBinding.stop.isVisible)
            assert(it.legalConsentBinding.record.isVisible)
        }
    }

    @Ignore
    @Test
    internal fun `playpause button should be enabled and stop button should be disabled when record is clicked`() {
        legalConsentScenario.onFragment {
            it.legalConsentBinding.record.performClick()
            assert(it.legalConsentBinding.playPause.isEnabled)
            assertFalse(it.legalConsentBinding.stop.isEnabled)
        }
    }

    @Ignore
    @Test
    internal fun `should start playing and recording when recording button is clicked`() {
        legalConsentScenario.onFragment {
            it.legalConsentBinding.record.performClick()
            verify { anyConstructed<AudioRecorder>().startPlaying() }
            verify { anyConstructed<AudioRecorder>().startRecording() }
        }
    }

    @Ignore
    @Test
    internal fun `should show recording in progress when recording button is clicked`() {
        legalConsentScenario.onFragment {
            assertFalse(it.legalConsentBinding.recordingInProgress.isVisible)
            it.legalConsentBinding.record.performClick()
            assert(it.legalConsentBinding.recordingInProgress.isVisible)
        }
    }

    @Ignore
    @Test
    internal fun `should disable back button while recording`() {
        legalConsentScenario.onFragment {
            assert(it.isCancelable)
            it.legalConsentBinding.record.performClick()
            assertFalse(it.isCancelable)
        }
    }

    @Ignore
    @Test
    internal fun `should change playpause text when playpause button is clicked`() {
        every { mediaPlayer.isPlaying } returns false

        legalConsentScenario.onFragment {
            it.legalConsentBinding.record.performClick() //isPlaying == true
            assert(it.legalConsentBinding.playPause.text == context.getString(R.string.pause))
            it.legalConsentBinding.playPause.performClick() //isPlaying == false
            assert(it.legalConsentBinding.playPause.text == context.getString(R.string.resume))
        }
    }

    @Ignore
    @Test
    internal fun `should pause playing and continue recording when playpause button is clicked`() {
        every { mediaPlayer.isPlaying } returns true

        legalConsentScenario.onFragment {
            it.legalConsentBinding.record.performClick()
            it.legalConsentBinding.playPause.performClick()

            verify { anyConstructed<AudioRecorder>().playPauseAudio() }
            verify(exactly = 0) { anyConstructed<AudioRecorder>().releaseRecording() }
        }
    }

    @Ignore
    @Test
    internal fun `should stop recording when stop button is clicked`() {
        legalConsentScenario = launchFragmentInContainer()
        legalConsentScenario.onFragment {
            it.legalConsentBinding.record.performClick()
            it.legalConsentBinding.stop.performClick()

            verify { anyConstructed<AudioRecorder>().releaseRecording() }
        }
    }

    @Ignore
    @Test
    internal fun `should close dialog when recording stops`() {
        legalConsentScenario.onFragment {
            it.legalConsentBinding.record.performClick()
            assert(it.dialog!!.isShowing)
            it.legalConsentBinding.stop.performClick()
            assertFalse(it.dialog?.isShowing!!)
        }
    }

    @Test
    @Ignore("Is entering the if statement within the onCompletionListener but the assertion is still failing")
    internal fun `should enable and change color for stop button when audioRecorder has finished playing`() {
        legalConsentScenario.onFragment {
            it.legalConsentBinding.record.performClick()

            every { anyConstructed<AudioRecorder>().hasFinishedPlaying() } returns MutableLiveData(
                true
            )

            it.onCreateView(it.layoutInflater, null, Bundle.EMPTY)

            assert(it.legalConsentBinding.stop.isEnabled)
            assert(it.legalConsentBinding.stop.background.equals(R.color.color_accent))
        }
    }
}
