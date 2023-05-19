package edu.upc.openmrs.activities.addeditpatient

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import edu.upc.R
import edu.upc.databinding.LegalConsentBinding
import edu.upc.openmrs.utilities.FileUtils
import kotlinx.android.synthetic.main.fragment_patient_info.*

class LegalConsentDialogFragment : DialogFragment() {

    private lateinit var legalConsentBinding: LegalConsentBinding
    private lateinit var audioRecorder: AudioRecorder

    private lateinit var recordButton: Button
    private lateinit var playPauseButton: Button
    private lateinit var stopButton: Button
    private var mFileName: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        legalConsentBinding = LegalConsentBinding.inflate(inflater, container, false)
        mFileName = context?.let { FileUtils.getRecordingFilePath(it) }
        audioRecorder = AudioRecorder(
            mFileName,
            requireContext(),
            FileUtils.getLegalConsentByLanguage(requireActivity())
        )
        setupButtons()
        listenForPlayCompletion()
        return legalConsentBinding.root
    }

    private fun listenForPlayCompletion() {
        audioRecorder.hasFinishedPlaying().observe(requireActivity()) {
            if (it == true) {
                stopButton.isEnabled = true
                playPauseButton.isEnabled = false
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        audioRecorder.stopRecording()
    }

    fun fileName(): String? = mFileName

    fun startRecordingNotification() {
        // TODO("Not yet implemented")
    }

    private fun setupButtons() {
        recordButton = legalConsentBinding.record
        playPauseButton = legalConsentBinding.playPause
        stopButton = legalConsentBinding.stop

        playPauseButton.isEnabled = false
        stopButton.isEnabled = false

        recordButton.setOnClickListener {
            audioRecorder.startRecording()
            audioRecorder.startPlaying()
            playPauseButton.setBackgroundResource(R.mipmap.pause)

            recordButton.isEnabled = false
            playPauseButton.isEnabled = true
        }

        playPauseButton.setOnClickListener {
            audioRecorder.playPauseAudio()
            playPauseButton.setBackgroundResource(if (audioRecorder.isPlaying()) R.mipmap.play_recording else R.mipmap.pause)
        }

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

    companion object {
        const val TAG: String = "legalConsent"
    }
}
