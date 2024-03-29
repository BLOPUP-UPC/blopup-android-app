package edu.upc.blopup

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaPlayer.create
import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.MutableLiveData

class AudioRecorder(private val outputFilePath: String?, context: Context, inputFileId: Int) {

    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private var isPlaying: Boolean = false
    private var isRecording: MutableLiveData<Boolean> = MutableLiveData(false)
    private var hasFinishedPlaying: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        mRecorder = createMediaRecorder(context)
        mPlayer = createMediaPlayer(context, inputFileId)
    }

    fun startRecording() {
        if (isRecording.value!!) {
            mRecorder?.stop()
        } else {
            mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            mRecorder?.setOutputFile(outputFilePath)
            mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mRecorder?.prepare()
            mRecorder?.start()
        }
        isRecording.value = !isRecording.value!!
    }

    fun stopRecording() {
        mPlayer?.reset()
        mPlayer?.release()

        mRecorder?.reset()
        mRecorder?.release()
        isPlaying = false
    }

    fun releaseRecording() {
        mRecorder?.stop()
        mRecorder?.release()
        mRecorder = null
    }

    fun startPlaying() {
        mPlayer!!.start()
        isPlaying = true
    }

    fun playPauseAudio() {
        try {
            if (mPlayer?.isPlaying == true) {
                mPlayer?.pause()
            } else {
                mPlayer?.start()
            }
            isPlaying = !isPlaying
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun isPlaying(): Boolean = isPlaying
    fun isRecording(): MutableLiveData<Boolean> = isRecording
    fun hasFinishedPlaying(): MutableLiveData<Boolean> = hasFinishedPlaying

    private fun createMediaPlayer(context: Context, inputFileId: Int): MediaPlayer? {
        val mediaPlayer = create(context, inputFileId)
        mediaPlayer.setOnCompletionListener {
            hasFinishedPlaying.value = true
        }
        return mediaPlayer
    }

    private fun createMediaRecorder(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(context) else MediaRecorder()
}
