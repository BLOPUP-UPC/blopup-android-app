package edu.upc.blopup

import android.text.TextUtils
import edu.upc.openmrs.utilities.FileUtils.removeLocalRecordingFile
import edu.upc.sdk.library.api.repository.RecordingRepository
import edu.upc.sdk.library.models.LegalConsent
import edu.upc.sdk.library.models.ResultType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class
RecordingHelper @Inject constructor(
    private val recordingRepository: RecordingRepository,
) {

    fun saveLegalConsent(legalConsent: LegalConsent) {

        if (TextUtils.isEmpty(legalConsent.patientIdentifier) || TextUtils.isEmpty(legalConsent.filePath)) return

        val response = recordingRepository.saveRecording(legalConsent)

        if (response == ResultType.RecordingSuccess) {
            removeLocalRecordingFile(legalConsent.filePath!!)
        }
    }
}
