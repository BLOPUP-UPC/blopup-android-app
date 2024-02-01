package edu.upc.openmrs.activities.addeditpatient

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomappbar.BottomAppBar
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.BuildConfig
import edu.upc.R
import edu.upc.blopup.AudioRecorder
import edu.upc.databinding.LegalConsentBinding
import edu.upc.openmrs.utilities.FileUtils
import edu.upc.openmrs.utilities.FileUtils.fileIsCreatedSuccessfully
import edu.upc.openmrs.utilities.FileUtils.getFileByLanguage
import edu.upc.openmrs.utilities.FileUtils.getRecordingFilePath
import edu.upc.openmrs.utilities.LanguageUtils
import edu.upc.openmrs.utilities.makeVisible
import java.util.Locale

@AndroidEntryPoint
class LegalConsentDialogFragment : DialogFragment() {

    lateinit var legalConsentBinding: LegalConsentBinding
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var recordButton: Button
    private lateinit var playPauseButton: TextView
    private lateinit var stopButton: BottomAppBar
    private lateinit var mFileName: String

    val viewModel: AddEditPatientViewModel by viewModels({requireParentFragment()})

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        legalConsentBinding = LegalConsentBinding.inflate(inflater, container, false)

        val language = arguments?.getString(ARG_LANGUAGE)
        val languageCode = context?.let { LanguageUtils.getLanguageCode(language, it) }

        getRecordingFilePath().also { mFileName = it }
        val inputFileId = if (BuildConfig.DEBUG) getFileByLanguage(requireActivity(), TAG, "test") else getFileByLanguage(requireActivity(), TAG, languageCode)
        audioRecorder = AudioRecorder(
            mFileName,
            requireContext(),
            inputFileId,
        )

        setLegalConsentWordingLanguage(languageCode!!)
        styleLegalConsentIntro(language!!)
        setupButtons()
        listenForPlayCompletion()
        isCancelableWhenNotRecording()
        return legalConsentBinding.root
    }

    private fun setLegalConsentWordingLanguage(language: String) {
        context?.let {
            val viewsAndResourceIds = listOf(
                Pair(legalConsentBinding.legalConsentWording, R.string.legal_consent),
                Pair(legalConsentBinding.bulletPoint1Text, R.string.first_bullet_point),
                Pair(legalConsentBinding.bulletPoint2Text, R.string.second_bullet_point),
                Pair(legalConsentBinding.bulletPoint3Text, R.string.third_bullet_point),
                Pair(legalConsentBinding.bulletPoint4Text, R.string.fourth_bullet_point),
                Pair(legalConsentBinding.legalConsentWordingBottom, R.string.bottom_text)
            )

            for ((view, resourceId) in viewsAndResourceIds) {
                val wording =
                    LanguageUtils.getLocaleStringResource(Locale(language), resourceId, it)
                view.text = wording
                if (language == "ar") {
                    legalConsentBinding.bulletPoint1Layout.layoutDirection =
                        View.LAYOUT_DIRECTION_RTL
                    legalConsentBinding.bulletPoint2Layout.layoutDirection =
                        View.LAYOUT_DIRECTION_RTL
                    legalConsentBinding.bulletPoint3Layout.layoutDirection =
                        View.LAYOUT_DIRECTION_RTL
                    legalConsentBinding.bulletPoint4Layout.layoutDirection =
                        View.LAYOUT_DIRECTION_RTL
                }
            }
        }
    }

    private fun styleLegalConsentIntro(language: String) {
        context?.let {
            val styledLegalConsentText = HtmlCompat.fromHtml(
                LanguageUtils.getLocaleStringResource(
                    resources.configuration.locales[0],
                    R.string.legal_consent_intro,
                    it
                ), HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            legalConsentBinding.legalConsentIntro.text = styledLegalConsentText
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
                playPauseButton.visibility = View.INVISIBLE
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
                viewModel.legalConsentFileName = mFileName
                viewModel.validateLegalConsent(true)
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
            if (viewModel.isLegalConsentValidLiveData.value == true) {
                FileUtils.removeLocalRecordingFile(viewModel.legalConsentFileName!!)
                viewModel.legalConsentFileName = ""
            }
            audioRecorder.startRecording()
            audioRecorder.startPlaying()

            playPauseButton.isVisible = true
            setMargin(playPauseButton)
            stopButton.isVisible = true
            stopButton.isClickable = false
            recordButton.visibility = View.GONE

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
