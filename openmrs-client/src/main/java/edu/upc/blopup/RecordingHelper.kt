package edu.upc.blopup

import edu.upc.openmrs.utilities.FileUtils
import edu.upc.openmrs.utilities.FileUtils.removeLocalRecordingFile
import edu.upc.sdk.library.api.repository.RecordingRepository
import edu.upc.sdk.library.models.LegalConsent
import edu.upc.sdk.library.models.ResultType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingHelper @Inject constructor(
    private val recordingRepository: RecordingRepository,
) {

    fun saveLegalConsent(legalConsent: LegalConsent) {

        val fullFilePath = FileUtils.getRootDirectory() + "/" + legalConsent.filePath

        val response = recordingRepository.saveRecording(legalConsent)

        response.subscribe {
            if (it == ResultType.RecordingSuccess) {
                removeLocalRecordingFile(fullFilePath)
            }
        }
    }
}
