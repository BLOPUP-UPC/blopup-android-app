package edu.upc.blopup

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaRecorder
import android.provider.MediaStore.Audio.Media
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import java.io.File

private const val TEST_FILE_PATH = "../testResources/recording.mp3"

@RunWith(RobolectricTestRunner::class)
class AudioRecorderTest{

    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var context: Context
    private lateinit var audioRecorder: AudioRecorder

    @Test
    fun `should set up file and start recording`() {
        mediaPlayer = mockk()
        context = ApplicationProvider.getApplicationContext()
        val file = File(TEST_FILE_PATH)

        mockkStatic(MediaPlayer::class)
        val slot = slot<OnCompletionListener>()

        every { MediaPlayer.create(any(), any<Int>()) } returns mediaPlayer
        every { mediaPlayer.setOnCompletionListener {  any() } } answers { }

        audioRecorder = AudioRecorder(TEST_FILE_PATH, context, 123)

        audioRecorder.startRecording()

        assert(audioRecorder.isRecording().value!!)
        assert(file.exists())
    }

    @Test
    fun stopRecording() {
    }

    @Test
    fun releaseRecording() {
    }

    @Test
    fun startPlaying() {
    }

    @Test
    fun playPauseAudio() {
    }
}