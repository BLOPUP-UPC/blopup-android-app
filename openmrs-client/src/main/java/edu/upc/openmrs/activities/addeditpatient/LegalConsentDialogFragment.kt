package edu.upc.openmrs.activities.addeditpatient

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import edu.upc.R
import edu.upc.databinding.LegalConsentBinding
import edu.upc.openmrs.utilities.FileUtils
import edu.upc.openmrs.utilities.NotificationUtil
import kotlinx.android.synthetic.main.fragment_patient_info.*

@RequiresApi(Build.VERSION_CODES.O)
class LegalConsentDialogFragment : DialogFragment() {

    private lateinit var legalConsentBinding: LegalConsentBinding
    private lateinit var audioRecorder: AudioRecorder

    private lateinit var recordButton: Button
    private lateinit var playPauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var mFileName: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        legalConsentBinding = LegalConsentBinding.inflate(inflater, container, false)
        mFileName = FileUtils.getRecordingFilePath(requireContext())
        audioRecorder = AudioRecorder(mFileName, requireContext(), FileUtils.getFileByLanguage(requireActivity(), TAG))
        setupButtons()
        listenForPlayCompletion()
        return legalConsentBinding.root
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        audioRecorder.stopRecording()
    }

    fun fileName(): String = mFileName

    private fun listenForPlayCompletion() {
        audioRecorder.hasFinishedPlaying().observe(requireActivity()) {
            if (it == true) {
                stopButton.isEnabled = true
                playPauseButton.isEnabled = false
            }
        }
    }

    private fun setupButtons() {
        recordButton = legalConsentBinding.record
        playPauseButton = legalConsentBinding.playPause
        stopButton = legalConsentBinding.stop

        playPauseButton.isEnabled = false
        stopButton.isEnabled = false

        startRecordingAndPlayingAudio()
        playPauseAudio()
        stopRecording()
    }

    private fun stopRecording() {
        stopButton.setOnClickListener {
            audioRecorder.releaseRecording()
            this.dismiss()

            if (FileUtils.fileIsCreatedSuccessfully(mFileName)) {
                val parent = parentFragment as AddEditPatientFragment
                parent.record_consent_imageButton.setImageResource(R.drawable.saved)
                parent.record_consent_imageButton.isEnabled = false
            }
        }
    }

    private fun playPauseAudio() {
        playPauseButton.setOnClickListener {
            audioRecorder.playPauseAudio()
            playPauseButton.setBackgroundResource(if (audioRecorder.isPlaying()) R.mipmap.play_recording else R.mipmap.pause)
        }
    }

    private fun startRecordingAndPlayingAudio() {
        recordButton.setOnClickListener {
            audioRecorder.startRecording()
            audioRecorder.startPlaying()
            playPauseButton.setBackgroundResource(R.mipmap.pause)

            recordButton.isEnabled = false
            playPauseButton.isEnabled = true
            NotificationUtil.showRecordingNotification("Recording in progress",getString(R.string.recording_info))
        }
    }

    companion object {
        const val TAG: String = "legal_consent"
    }
}
