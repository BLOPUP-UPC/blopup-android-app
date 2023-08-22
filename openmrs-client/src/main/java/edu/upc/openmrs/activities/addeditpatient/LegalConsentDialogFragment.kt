package edu.upc.openmrs.activities.addeditpatient

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomappbar.BottomAppBar
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
    private lateinit var playPauseButton: TextView
    private lateinit var stopButton: BottomAppBar
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
        audioRecorder = AudioRecorder(
            mFileName,
            requireContext(),
            getFileByLanguage(requireActivity(), TAG, languageCode)
        )

        setLegalConsentWordingLanguage(languageCode!!)
        setupButtons()
        listenForPlayCompletion()
        isCancelableWhenNotRecording()
        return legalConsentBinding.root
    }

    private fun setLegalConsentWordingLanguage(language: String) {
        context?.let {
            val viewsAndResourceIds = listOf(
                Pair(legalConsentBinding.legalConsentWording, R.string.legal_consent),
                Pair(legalConsentBinding.firstBulletPoint, R.string.first_bullet_point),
                Pair(legalConsentBinding.secondBulletPoint, R.string.second_bullet_point),
                Pair(legalConsentBinding.thirdBulletPoint, R.string.third_bullet_point),
                Pair(legalConsentBinding.forthBulletPoint, R.string.fourth_bullet_point),
                Pair(legalConsentBinding.legalConsentWordingBottom, R.string.bottom_text)
            )

            for ((view, resourceId) in viewsAndResourceIds) {
                val wording = LanguageUtils.getLocaleStringResource(Locale(language), resourceId, it)
                if(view == legalConsentBinding.legalConsentWording){
                    val styledLegalConsentText = HtmlCompat.fromHtml(wording, Html.FROM_HTML_MODE_LEGACY)
                    legalConsentBinding.legalConsentWording.text = styledLegalConsentText
                } else {
                    view.text = wording
                }
            }
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
                playPauseButton.visibility = View.GONE
                stopButton.isClickable = true
                stopButton.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.color_accent, null))
            }
        }
    }

    private fun setupButtons() {
        recordButton = legalConsentBinding.record
        playPauseButton = legalConsentBinding.playPause
        stopButton = legalConsentBinding.stop

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
            playPauseButton.setText(if (!audioRecorder.isPlaying()) R.string.resume else R.string.pause)
        }
    }

    private fun startRecordingAndPlayingAudio() {
        recordButton.setOnClickListener {
            audioRecorder.startRecording()
            audioRecorder.startPlaying()

            playPauseButton.isVisible = true
            setMargin(playPauseButton)
            stopButton.isVisible = true
            stopButton.isClickable = false
            recordButton.visibility = View.GONE
            legalConsentBinding.legalConsentContainer.background =
                context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.orange_border) }

            legalConsentBinding.recordingInProgress.makeVisible()

            val scrollView = legalConsentBinding.scrollWording
            val params = scrollView.layoutParams
            params.height = resources.getDimensionPixelSize(R.dimen.legal_consent)
            scrollView.layoutParams = params
        }
    }

    private fun setMargin(textView: TextView) {
        val densityOperator = context?.resources?.displayMetrics?.density?.toInt()

        val margin = textView.layoutParams as LinearLayout.LayoutParams
        margin.bottomMargin = densityOperator!! * 45
        margin.topMargin = densityOperator * 25

        textView.layoutParams = margin
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
