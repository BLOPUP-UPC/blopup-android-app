package edu.upc.blopup

import edu.upc.BuildConfig
import edu.upc.openmrs.utilities.FileUtils
import edu.upc.openmrs.utilities.FileUtils.removeLocalRecordingFile
import edu.upc.sdk.library.api.repository.RecordingRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingHelper @Inject constructor(
    private val recordingRepository: RecordingRepository,
    private val patientDAO: PatientDAO
) {

    fun saveLegalConsent(patient: Patient) {
        val value = patient
            .attributes
            .first { it.attributeType?.uuid == BuildConfig.LEGAL_CONSENT_ATTRIBUTE_TYPE_UUID }
            .value

        val fullFilePath = FileUtils.getRootDirectory() + "/" + value

        val response = recordingRepository.saveRecording(fullFilePath)

        response.subscribe {
            if (it == "OK") {
                patient.isLegalConsentSynced = true
                patientDAO.updatePatient(patient)
                removeLocalRecordingFile(fullFilePath)
            }
        }
    }
}