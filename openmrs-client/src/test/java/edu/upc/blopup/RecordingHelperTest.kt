package edu.upc.blopup

import edu.upc.BuildConfig
import edu.upc.openmrs.utilities.FileUtils
import edu.upc.sdk.library.api.repository.RecordingRepository
import edu.upc.sdk.library.dao.PatientDAO
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.PersonAttribute
import edu.upc.sdk.library.models.PersonAttributeType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import rx.Observable

class RecordingHelperTest {

    private lateinit var patient: Patient
    private lateinit var recordingHelper: RecordingHelper
    private lateinit var recordingRepository: RecordingRepository
    private lateinit var patientDAO: PatientDAO
    private lateinit var fullFilePath: String

    @Before
    fun setUp() {
        val legalConsentAttributeType = PersonAttributeType().apply {
            uuid = BuildConfig.LEGAL_CONSENT_ATTRIBUTE_TYPE_UUID
        }
        patient = Patient().apply {
            attributes = listOf( PersonAttribute().apply {
                attributeType = legalConsentAttributeType
                value = FILE_NAME
            })
        }
        recordingRepository = mockk()

        patientDAO = mockk()
        every { patientDAO.updatePatient(patient) } returns true

        recordingHelper = RecordingHelper(recordingRepository, patientDAO)

        mockkStatic(FileUtils::class)

        every { FileUtils.getRootDirectory() } returns ROOT_DIRECTORY

        fullFilePath = "$ROOT_DIRECTORY/$FILE_NAME"
    }

    @Test
    fun `should save legalConsent to Repository`() {

        every { recordingRepository.saveRecording(fullFilePath) } returns Observable.just("OK")

        recordingHelper.saveLegalConsent(patient)

        verify { recordingRepository.saveRecording(fullFilePath) }
    }

    @Test
    fun `should set patient legalConsentSynced to true when successful`(){
        patient.isLegalConsentSynced = true

        every { recordingRepository.saveRecording(fullFilePath) } returns Observable.just("OK")

        recordingHelper.saveLegalConsent(patient)

        verify {  patientDAO.updatePatient(patient) }
    }

    @Test
    fun `shouldn't update patient legalConsentSynced when not successful`() {
        every { recordingRepository.saveRecording(fullFilePath) } returns Observable.just("Error")

        recordingHelper.saveLegalConsent(patient)

        verify(exactly = 0) { patientDAO.updatePatient(patient) }
    }

    @Test
    fun `should remove recording file from local when successful`(){

        every { recordingRepository.saveRecording(fullFilePath) } returns Observable.just("OK")

        recordingHelper.saveLegalConsent(patient)

        verify {  FileUtils.removeLocalRecordingFile(fullFilePath) }
    }

    companion object {
        private const val FILE_NAME = "20230525_123926_.mp3"
        private const val ROOT_DIRECTORY = "/storage/emulated/0/Android/data/edu.upc/files/DCIM"
}
}