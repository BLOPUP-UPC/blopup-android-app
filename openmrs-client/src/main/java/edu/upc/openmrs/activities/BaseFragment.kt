package edu.upc.openmrs.activities

import androidx.fragment.app.Fragment
import edu.upc.R
import edu.upc.openmrs.utilities.LanguageUtils.setupLanguage
import edu.upc.openmrs.utilities.NotificationUtil.showRecordingNotification

abstract class BaseFragment : Fragment() {
    val isActive: Boolean get() = isAdded

    open fun startRecordingNotification() {
        showRecordingNotification(
            getString(R.string.recording_inprogress),
            getString(R.string.recording_info)
        )
    }

    override fun onResume() {
        super.onResume()
        setupLanguage(resources)
    }
}
