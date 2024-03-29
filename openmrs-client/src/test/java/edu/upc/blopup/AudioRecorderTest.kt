package edu.upc.blopup

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.*
import junit.framework.TestCase.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_FILE_PATH = "../testResources/recording.mp3"

@RunWith(AndroidJUnit4::class)
class AudioRecorderTest {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var context: Context
    private lateinit var audioRecorder: AudioRecorder

    @Before
    fun setUp() {
        mediaPlayer = mockk(relaxUnitFun = true)
        context = ApplicationProvider.getApplicationContext()

        mockkStatic(MediaPlayer::class)
        mockkConstructor(MediaRecorder::class)
        every { anyConstructed<MediaRecorder>().start() } just Runs
        every { anyConstructed<MediaRecorder>().setOutputFile(TEST_FILE_PATH) } just Runs

        every { MediaPlayer.create(any(), any<Int>()) } returns mediaPlayer

        audioRecorder = AudioRecorder(TEST_FILE_PATH, context, 123)
    }

    @Test
    fun `should set up file and start recording`() {
        audioRecorder.startRecording()

        assert(audioRecorder.isRecording().value!!)
        verify {
            anyConstructed<MediaRecorder>().setOutputFile(TEST_FILE_PATH)
            anyConstructed<MediaRecorder>().start()
        }
    }

    @Test
    fun `should start playing file`() {
        audioRecorder.startPlaying()

        assert(audioRecorder.isPlaying())
        verify { mediaPlayer.start() }
    }

    @Test
    fun `should pause file if already playing`() {
        every { mediaPlayer.isPlaying } returns true
        audioRecorder.startPlaying()

        audioRecorder.playPauseAudio()

        assertFalse(audioRecorder.isPlaying())
        verify { mediaPlayer.pause() }
    }
}
