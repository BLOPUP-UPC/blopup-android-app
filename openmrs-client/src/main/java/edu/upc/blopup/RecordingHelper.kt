package edu.upc.blopup

import android.util.Log
import edu.upc.sdk.library.api.repository.RecordingRepository
import java.io.File
import javax.inject.Inject
import edu.upc.BuildConfig
import edu.upc.openmrs.utilities.FileUtils
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient

class RecordingHelper @Inject constructor(
    private val recordingRepository: RecordingRepository,
    private val patient: Patient,
    private val patientDAO: PatientDAO
) {

    fun saveLegalConsent() {
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
            }
        }
    }

//    fun saveLegalConsent(): LiveData<ResultType> {
//        val result = MutableLiveData<ResultType>()
//
//        patient.attributes?.forEach { attribute ->
//            if (attribute.attributeType?.uuid == BuildConfig.LEGAL_CONSENT_ATTRIBUTE_TYPE_UUID) {
//                val file = File(FileUtils.getRootDirectory() + "/" + attribute.value)
//
//                addSubscription(recordingRepository.saveRecording(recording)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(
//                        {
//                            result.value = ResultType.RecordingSuccess
//                            patient.isLegalConsentSynced = true
//                            removeLocalRecordingFile(file)
//                        },
//                        { result.value = ResultType.RecordingError }
//                    )
//                )
//            }
//        }
//        return result
//    }

    private fun removeLocalRecordingFile(file: File) {
        if (file.exists()) {
            try {
                file.delete()
            } catch (e: SecurityException) {
                Log.e("file", "Error deleting file: ${file.absolutePath}", e)
            }
        } else {
            Log.d("file", "File does not exist: ${file.absolutePath}")
        }
    }
}