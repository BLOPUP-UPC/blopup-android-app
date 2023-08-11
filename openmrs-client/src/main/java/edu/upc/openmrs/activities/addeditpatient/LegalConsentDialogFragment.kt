package edu.upc.openmrs.activities.addeditpatient

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import edu.upc.R
import edu.upc.blopup.AudioRecorder
import edu.upc.databinding.LegalConsentBinding
import edu.upc.openmrs.utilities.FileUtils.fileIsCreatedSuccessfully
import edu.upc.openmrs.utilities.FileUtils.getFileByLanguage
import edu.upc.openmrs.utilities.FileUtils.getRecordingFilePath
import edu.upc.openmrs.utilities.LanguageUtils
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeVisible
import kotlinx.android.synthetic.main.fragment_patient_info.*
import java.util.*


class LegalConsentDialogFragment : DialogFragment() {

    private lateinit var legalConsentBinding: LegalConsentBinding
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var recordButton: Button
    private lateinit var playPauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var mFileName: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        legalConsentBinding = LegalConsentBinding.inflate(inflater, container, false)

        val language = arguments?.getString(ARG_LANGUAGE)
        val languageCode = context?.let { LanguageUtils.getLanguageCode(language, it) }

        getRecordingFilePath().also { mFileName = it }
        audioRecorder = AudioRecorder(mFileName, requireContext(), getFileByLanguage(requireActivity(), TAG, languageCode))

        setLegalConsentWordingLanguage(languageCode!!)
        setupButtons()
        listenForPlayCompletion()
        isCancelableWhenNotRecording()
        return legalConsentBinding.root
    }

    private fun setLegalConsentWordingLanguage(language: String) {
        val legalConsentWording = legalConsentBinding.legalConsentWording

        legalConsentWording.text = context?.let {
            LanguageUtils.getLocaleStringResource(Locale(language), R.string.legal_consent, it)
        }
    }

    override fun onStart() {
        super.onStart()
        setDialogWidthAndHeight()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        audioRecorder.stopRecording()
    }

    fun fileName(): String = mFileName

    private fun setDialogWidthAndHeight() {
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)
    }

    private fun isCancelableWhenNotRecording() {
        audioRecorder.isRecording().observe(requireActivity()) {
            if (it) {
                isCancelable = false
            }
        }
    }

    private fun listenForPlayCompletion() {

        audioRecorder.hasFinishedPlaying().observe(requireActivity()) {
            if (it) {
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

            if (fileIsCreatedSuccessfully(mFileName)) {
                val parent = parentFragment
                parent?.record_consent_saved?.isEnabled = false
                parent?.record_consent_saved?.makeVisible()
                parent?.language_spinner?.isEnabled = false
                parent?.record_legal_consent?.makeGone()
            }
        }
    }

    private fun playPauseAudio() {
        playPauseButton.setOnClickListener {
            audioRecorder.playPauseAudio()
            playPauseButton.setBackgroundResource(if (!audioRecorder.isPlaying()) R.mipmap.play_recording else R.mipmap.pause)
        }
    }

    private fun startRecordingAndPlayingAudio() {
        recordButton.setOnClickListener {
            audioRecorder.startRecording()
            audioRecorder.startPlaying()
            playPauseButton.setBackgroundResource(R.mipmap.pause)

            recordButton.isEnabled = false
            playPauseButton.isEnabled = true

            legalConsentBinding.recordingInProgress.makeVisible()
        }
    }

    companion object {
        const val TAG: String = "legal_consent"
        private const val ARG_LANGUAGE = "language"

        fun newInstance(language: String): LegalConsentDialogFragment {
            val fragment = LegalConsentDialogFragment()
            val args = Bundle()
            args.putString(ARG_LANGUAGE, language)
            fragment.arguments = args
            return fragment
        }
    }
}
