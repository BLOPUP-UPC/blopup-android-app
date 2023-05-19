package edu.upc.openmrs.activities.addeditpatient

import android.content.DialogInterface
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import edu.upc.R
import edu.upc.databinding.LegalConsentBinding
import edu.upc.openmrs.utilities.FileUtils
import kotlinx.android.synthetic.main.activity_form_display.*
import kotlinx.android.synthetic.main.fragment_patient_info.*

class LegalConsentDialogFragment : DialogFragment(R.layout.legal_consent) {

    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private var isPlaying: Boolean = false
    private var isRecording: Boolean = false
    private var recordButton: Button? = null
    private var playPauseButton: Button? = null
    private var stopButton: Button? = null
    var mFileName: String? = null

    private val viewModel: LegalConsentViewModel by viewModels()

    override fun onStart() {
        super.onStart()

        setupButtons()
        mFileName = context?.let { FileUtils.getRecordingFilePath(it) }
        mRecorder = createMediaRecorder()
        mPlayer = createMediaPlayer()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        stopRecording()

        if (FileUtils.fileIsCreatedSuccessfully(mFileName)) {
            val parent = parentFragment as AddEditPatientFragment
            parent.record_consent_imageButton.setImageResource(R.drawable.saved)
            parent.record_consent_imageButton.isEnabled = false
        }
    }

    private fun setupButtons() {
        val legalConsentBinding = LegalConsentBinding.inflate(layoutInflater, container, false)

        recordButton = legalConsentBinding.record
        playPauseButton = legalConsentBinding.playPause
        stopButton = legalConsentBinding.stop

        playPauseButton?.isEnabled = false
        stopButton?.isEnabled = false

        playPauseButton?.setOnClickListener {
            playPauseAudio()
        }

        stopButton?.setOnClickListener {
            mRecorder?.stop()
            mRecorder?.release()
            mRecorder = null
            this.dismiss()
        }

        recordButton?.setOnClickListener {
            startRecording()
            startPlaying()

            recordButton?.isEnabled = false
            playPauseButton?.isEnabled = true
        }
    }

    private fun createMediaRecorder() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        context?.let { MediaRecorder(it) } else MediaRecorder()

    private fun createMediaPlayer(): MediaPlayer? {
        val mediaPLayer = MediaPlayer.create(context, FileUtils.getLegalConsentByLanguage(activity))
        mediaPLayer.setOnCompletionListener {
            stopButton?.isEnabled = true
        }
        return mediaPLayer
    }

    private fun startPlaying() {
        mPlayer!!.start()
        playPauseButton?.setBackgroundResource(R.mipmap.pause)
    }

    private fun startRecording() {
        if (isRecording) {
            mRecorder?.stop()
        } else {
            mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mRecorder?.setOutputFile(mFileName)
            mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mRecorder?.prepare()
            mRecorder?.start()
            startRecordingNotification()
        }
        isRecording = !isRecording
    }

    private fun startRecordingNotification() {
        TODO("Not yet implemented")
    }

    private fun playPauseAudio() {
        try {
            if (mPlayer?.isPlaying == true) {
                mPlayer?.pause()
            } else {
                mPlayer?.start()
            }
            isPlaying = !isPlaying
            playPauseButton?.setBackgroundResource(if (isPlaying) R.mipmap.play_recording else R.mipmap.pause)
        } catch (exception: Exception) {
            exception.printStackTrace();
        }
    }

    private fun stopRecording() {
        mPlayer?.reset()
        mPlayer?.release()

        mRecorder?.reset()
        mRecorder?.release()
        isPlaying = false
    }

    companion object {
        const val TAG: String = "legalConsent"
    }
}
