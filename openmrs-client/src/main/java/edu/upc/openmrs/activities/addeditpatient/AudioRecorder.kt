package edu.upc.openmrs.activities.addeditpatient

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.widget.Button
import edu.upc.R
import edu.upc.openmrs.utilities.FileUtils

class AudioRecorder (private val fileName: String?, context: Context, activity: Activity, stopButton: Button, playPauseButton: Button){

    private var mRecorder: MediaRecorder? = null
    private var isRecording: Boolean = false
    private var mPlayer: MediaPlayer? = null
    private var isPlaying: Boolean = false

    init {
        mRecorder = createMediaRecorder(context)
        mPlayer = createMediaPlayer(context, activity, stopButton , playPauseButton)
    }

    private fun createMediaRecorder(context: Context) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        MediaRecorder(context) else MediaRecorder()

    fun startRecording() {
        if (isRecording) {
            mRecorder?.stop()
        } else {
            mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mRecorder?.setOutputFile(fileName)
            mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mRecorder?.prepare()
            mRecorder?.start()
//            startRecordingNotification()
        }
        isRecording = !isRecording
    }

    private fun startRecordingNotification() {
        // TODO("Not yet implemented")
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

    private fun createMediaPlayer(context: Context, activity: Activity, stopButton: Button, playPauseButton: Button): MediaPlayer? {
        val mediaPLayer = MediaPlayer.create(context, FileUtils.getLegalConsentByLanguage(activity))
        mediaPLayer.setOnCompletionListener {
            stopButton.isEnabled = true
            playPauseButton.isEnabled = false
        }
        return mediaPLayer
    }

    fun startPlaying(playPauseButton: Button) {
        mPlayer!!.start()
        playPauseButton.setBackgroundResource(R.mipmap.pause)
    }

    fun playPauseAudio(playPauseButton: Button) {
        try {
            if (mPlayer?.isPlaying == true) {
                mPlayer?.pause()
            } else {
                mPlayer?.start()
            }
            isPlaying = !isPlaying
            playPauseButton.setBackgroundResource(if (isPlaying) R.mipmap.play_recording else R.mipmap.pause)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}